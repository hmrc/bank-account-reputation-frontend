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

package featureswitch.frontend.controllers

import config.AppConfig
import controllers.BaseController
import featureswitch.frontend.services.FeatureSwitchRetrievalService
import featureswitch.frontend.views.html.feature_switch
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.AuthConnector

import javax.inject.Inject
import scala.concurrent.ExecutionContext


class FeatureSwitchFrontendController @Inject()(
                                                 authConnector: AuthConnector,
                                                 featureSwitchService: FeatureSwitchRetrievalService,
                                                 notAuthorised: views.html.NotAuthorised,
                                                 featureSwitchView: feature_switch,
                                                 mcc: MessagesControllerComponents
                                               )(implicit ec: ExecutionContext, appConfig: AppConfig)
  extends BaseController(authConnector, notAuthorised, mcc) {

  def show(): Action[AnyContent] = Action.async {
    implicit req =>
      strideAuth { _ =>
        featureSwitchService.retrieveFeatureSwitches().map {
          featureSwitches =>
            Ok(featureSwitchView(featureSwitches, routes.FeatureSwitchFrontendController.submit()))
        }
      }
  }

  def submit(): Action[Map[String, Seq[String]]] = Action.async(parse.formUrlEncoded) {
    implicit req =>
      strideAuth { _ =>
        featureSwitchService.updateFeatureSwitches(req.body.keys).map { _ =>
          Redirect(routes.FeatureSwitchFrontendController.show())
        }
      }
  }

}
