/*
 * Copyright 2019 HM Revenue & Customs
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

import config.FrontendAppConfig
import connector.BackendConnector
import javax.inject._
import models._
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions, NoActiveSession}
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class BarsController @Inject()(
                                connector: BackendConnector,
                                val authConnector: AuthConnector,
                                mcc: MessagesControllerComponents,
                                indexView: views.html.index,
                                metadataView: views.html.metadata,
                                metadataResultView: views.html.metadataResult,
                                modckeckView: views.html.modcheck,
                                modckeckResultView: views.html.modcheckResult,
                                validateView: views.html.validate,
                                validationResultView: views.html.validationResult,
                                assessmentView: views.html.assess,
                                assessmentResultView: views.html.assessmentResult,
                                error: views.html.error_template
                              )
                              (implicit ec: ExecutionContext, appConfig: FrontendAppConfig) extends FrontendController(mcc)
  with AuthorisedFunctions with AuthRedirects with I18nSupport {

  val config = appConfig.configuration
  val env = appConfig.environment

  def index(): Action[AnyContent] = Action.async { implicit request =>
    strideAuth {
      if (!assessmentEnabled) {
        Future.successful(Ok(validateView(accountForm)))
      } else {
        Future.successful(Ok(indexView()))
      }
    }
  }

  private def strideAuth(f: => Future[Result])(implicit request: Request[_]) = {
    if (strideAuthEnabled) {
      authorised() {
        f
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
        case _ => Ok(error("Not authorised", "Not authorised", "Sorry, not authorised"))
      }
    } else {
      f
    }
  }

  def metadataLookup: Action[AnyContent] = Action {

    implicit request =>

      Ok(metadataView(sortCodeForm))
  }

  def metadata: Action[AnyContent] = Action.async {

    implicit request =>

      sortCodeForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest(metadataView(formWithErrors)))
        },
        account => {
          connector.metadata(account.sortCode)
            .map(result =>
              Ok(metadataResultView(account, result))
            )
        }
      )
  }

  def modChecking: Action[AnyContent] = Action {

    implicit req =>

      Ok(modckeckView(accountForm))
  }

  def modCheck: Action[AnyContent] = Action.async {

    implicit request =>

      accountForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest(modckeckView(formWithErrors)))
        },
        account => {
          connector.modcheck(AccountDetails(Account(account.sortCode, account.accountNumber)))
            .map(result =>
              Ok(modckeckResultView(account, result))
            )
        }
      )
  }

  def validation: Action[AnyContent] = Action {

    implicit req =>

      Ok(validateView(accountForm))
  }

  def validate: Action[AnyContent] = Action.async {
    implicit request =>

      strideAuth {
        accountForm.bindFromRequest.fold(
          formWithErrors => {
            Future.successful(BadRequest(validateView(formWithErrors)))
          },
          account => {
            val validationFuture: Future[ValidationResult] = {
              if (!account.accountNumber.isEmpty) {
                connector.validate(AccountDetails(Account(account.sortCode, account.accountNumber)))
              } else {
                Future.successful(ValidationResult(false, "N/A", "N/A"))
              }
            }

            val result = for {
              metadata <- connector.metadata(account.sortCode)
              validation <- validationFuture
            } yield (metadata, validation)
            result.map {
              res => Ok(validationResultView(account, res._1, res._2))
            } recover {
              case e: uk.gov.hmrc.http.NotFoundException => {
                Logger.warn("Failed to retrieve bank details: " + e.toString)
                BadRequest(validateView(accountForm.withError("sortCode", "Failed to retrieve bank details for sort code " + account.sortCode)))
              }
            }
          }
        )
      }
  }

  def assessment: Action[AnyContent] = Action {

    implicit req =>

      if (!assessmentEnabled) {
        Logger.warn("Attempted access to assessment but feature is disabled")
        NotFound
      } else {
        Ok(assessmentView(inputForm))
      }
  }

  def assess: Action[AnyContent] = Action.async {

    implicit request =>

      if (!assessmentEnabled) {
        Logger.warn("Attempted access to assessment but feature is disabled")
        Future.successful(NotFound)
      } else {
        inputForm.bindFromRequest.fold(
          formWithErrors => {
            Future.successful(BadRequest(assessmentView(formWithErrors)))
          },
          input => {
            connector.assess(input.input)
              .map(result =>
                Ok(assessmentResultView(input, result))
              )
          }
        )
      }
  }

  private def assessmentEnabled: Boolean = appConfig.isAssessmentEnabled

  private def strideAuthEnabled: Boolean = appConfig.isStrideAuthEnabled
}
