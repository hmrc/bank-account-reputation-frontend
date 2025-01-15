/*
 * Copyright 2025 HM Revenue & Customs
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
import models.AuthRedirects
import play.api.i18n.I18nSupport
import play.api.mvc.{MessagesControllerComponents, Request, Result}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.auth.core.retrieve.{Credentials, ~}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BaseController @Inject()(
                                val authConnector: AuthConnector,
                                notAuthorised: views.html.NotAuthorised,
                                mcc: MessagesControllerComponents
                              )(implicit ec: ExecutionContext, appConfig: AppConfig)
  extends FrontendController(mcc) with AuthorisedFunctions with AuthRedirects with I18nSupport {

  case class RetrievalsToAudit(credentials: Option[Credentials],
                               allEnrolments: Enrolments,
                               affinityGroup: Option[AffinityGroup],
                               internalId: Option[String],
                               externalId: Option[String],
                               credentialStrength: Option[String],
                               agentCode: Option[String],
                               profile: Option[String],
                               groupProfile: Option[String],
                               emailVerified: Option[Boolean],
                               credentialRole: Option[CredentialRole])

  val config: Configuration = appConfig.config

  val env: Environment = appConfig.env

  val retrievalsToAudit = credentials and allEnrolments and affinityGroup and internalId and externalId and credentialStrength and agentCode and profile and groupProfile and emailVerified and credentialRole

  def strideAuth(f: Option[RetrievalsToAudit] => Future[Result])(implicit request: Request[_]): Future[Result] = {
    if (appConfig.isStrideAuthEnabled) {
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

}
