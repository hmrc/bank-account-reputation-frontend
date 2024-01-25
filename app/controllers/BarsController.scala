/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import config.AppConfig
import connector.{BankAccountReputationConnector, BarsAssessErrorResponse}
import models._
import play.api.{Configuration, Environment, Logger}
import play.api.i18n._
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Retrieval, ~}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class BarsController @Inject()(
                                connector: BankAccountReputationConnector,
                                val authConnector: AuthConnector,
                                mcc: MessagesControllerComponents,
                                auditConnector: AuditConnector,
                                accessibilityView: views.html.accessibility,
                                verifyView: views.html.verify,
                                verifyResultView: views.html.verifyResult,
                                notAuthorised: views.html.NotAuthorised
                              )(implicit ec: ExecutionContext, appConfig: AppConfig)
  extends FrontendController(mcc) with AuthorisedFunctions with AuthRedirects with I18nSupport {

  private val logger = Logger(this.getClass)

  val retrievalsToAudit: Retrieval[Option[Credentials] ~ Enrolments ~ Option[AffinityGroup] ~ Option[String] ~ Option[String] ~ Option[String] ~ Option[String] ~ Option[String] ~ Option[String] ~ Option[Boolean] ~ Option[CredentialRole]] = credentials and allEnrolments and affinityGroup and internalId and externalId and credentialStrength and agentCode and profile and groupProfile and emailVerified and credentialRole

  case class RetrievalsToAudit(credentials: Option[Credentials], allEnrolments: Enrolments,
                               affinityGroup: Option[AffinityGroup], internalId: Option[String],
                               externalId: Option[String], credentialStrength: Option[String],
                               agentCode: Option[String], profile: Option[String], groupProfile: Option[String],
                               emailVerified: Option[Boolean], credentialRole: Option[CredentialRole])

  def accessibilityStatement(): Action[AnyContent] = Action.async {
    Future.successful(Ok(accessibilityView()))
  }

  private def strideAuth(f: Option[RetrievalsToAudit] => Future[Result])(implicit request: Request[_]) = {
    if (strideAuthEnabled) {
      authorised(Enrolment(AppConfig.srsRoleName)).retrieve(retrievalsToAudit) {
        case credentials ~ allEnrolments ~ affinityGroup ~ internalId ~ externalId ~ credentialStrength ~ agentCode ~ profile ~ groupProfile ~ emailVerified ~ credentialRole =>
          f(Some(RetrievalsToAudit(credentials, allEnrolments, affinityGroup, internalId, externalId, credentialStrength, agentCode, profile, groupProfile, emailVerified, credentialRole)))
      } recover {
        case _: NoActiveSession =>
          toStrideLogin {
            if (appConfig.isLocal) {
              s"http://${request.host}${request.uri}"
            }
            else {
              s"${request.uri}"
            }
          }
        case _ => Ok(notAuthorised())
      }
    } else {
      f(None)
    }
  }

  def redirectToVerify: Action[AnyContent] = Action {
    Redirect(controllers.routes.BarsController.getVerify)
  }

  def redirectToVerifySecure: Action[AnyContent] = Action {
    Redirect(controllers.routes.BarsController.getVerifySecure)
  }

  def getVerify: Action[AnyContent] = Action.async {
    implicit req => Future.successful(Ok(verifyView(inputForm, secure = false)))
  }

  def getVerifySecure: Action[AnyContent] = Action.async {
    implicit req =>
      strideAuth { r =>
        Future.successful(Ok(verifyView(inputForm, secure = true)))
      }
  }

  def postVerify: Action[AnyContent] = Action.async {
    implicit request => handlePost()
  }

  def postVerifySecure: Action[AnyContent] = Action.async {
    implicit request =>
      strideAuth { retrievals =>
        handlePost(secure = true, retrievals)
      }
  }

  private def handlePost(secure: Boolean = false, retrievals: Option[RetrievalsToAudit] = None)(implicit req: MessagesRequest[AnyContent]): Future[Result] = {
    inputForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(BadRequest(verifyView(formWithErrors, secure)))
      },
      input => {
        val pid = retrievals.flatMap(r => r.credentials.collect {
          case Credentials(pid, "PrivilegedApplication") => pid
        })

        for {
          metadata <- connector.metadata(input.input.account.sortCode)
          assess <- if (input.input.account.accountNumber.isDefined && metadata.isDefined) {
            input.input.account.accountType match {
              case Some("personal") => connector.assessPersonal(
                input.input.subject.name.getOrElse("N/A"),
                input.input.account.sortCode,
                input.input.account.accountNumber.get,
                "bank-account-reputation-frontend").map(Some(_))
              case _ => connector.assessBusiness(
                input.input.subject.name.getOrElse("N/A"),
                input.input.account.sortCode,
                input.input.account.accountNumber.get, None,
                "bank-account-reputation-frontend").map(Some(_))
            }
          }
          else {
            Future.successful(None)
          }
        }
        yield {
          val dataEvent = DataEvent(
            appConfig.appName,
            "BankAccountReputationFrontendLookup",
            detail = Map[String, String](
              "PID" -> pid.getOrElse(""),
              "DeviceId" -> hc.deviceID.getOrElse(""),
              "SortCode" -> input.input.account.sortCode,
              "AccountNumber" -> input.input.account.accountNumber.getOrElse(""),
              "AccountName" -> input.input.subject.name.getOrElse("N/A"),
              "AccountType" -> input.input.account.accountType.getOrElse("business")
            ) ++ AuditDetail.from("Retrievals", retrievals)
              ++ AuditDetail.from("Response.metadata", metadata)
              ++ AuditDetail.from("Response.assess", assess))

          auditConnector.sendEvent(dataEvent)

          (metadata, assess) match {
            case (None, None) => BadRequest(verifyView(inputForm.fill(input).withError("input.account.sortCode", "bars.label.sortCodeNotFound"), secure))
            case (Some(m), None) => Ok(verifyResultView(input.input.account, m, None, secure))
            case (Some(m), Some(Failure(_))) => Ok(verifyResultView(input.input.account, m, Some(BarsAssessErrorResponse()), secure))
            case (Some(m), Some(Success(a))) => Ok(verifyResultView(input.input.account, m, Some(a), secure))
          }
        }
      }
    )
  }

  private def strideAuthEnabled: Boolean = appConfig.isStrideAuthEnabled

  val config: Configuration = appConfig.config
  val env: Environment = appConfig.env
}
