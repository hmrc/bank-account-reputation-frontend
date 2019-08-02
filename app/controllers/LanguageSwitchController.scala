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

import com.google.inject.Inject
import config.FrontendAppConfig
import connector.BackendConnector
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.language.LanguageUtils

import scala.concurrent.ExecutionContext

// TODO, upstream this into play-language
class LanguageSwitchController @Inject()(
                                          connector: BackendConnector,
                                          mcc: MessagesControllerComponents,
                                          indexView: views.html.index,
                                          validateView: views.html.validate,
                                          validationResultView: views.html.validationResult
                                        )
                                        (implicit ec: ExecutionContext, appConfig: FrontendAppConfig) extends FrontendController(mcc) with I18nSupport {

  private def langToCall(lang: String): (String) => Call = appConfig.routeToSwitchLanguage

  private def fallbackURL: String = routes.BarsController.index().url

  private def languageMap: Map[String, Lang] = appConfig.languageMap

  def switchToLanguage(language: String): Action[AnyContent] = Action {
    implicit request =>
      val enabled = isWelshEnabled
      val lang = if (enabled) {
        languageMap.getOrElse(language, LanguageUtils.getCurrentLang)
      } else {
        Lang("en")
      }
      val redirectURL = request.headers.get(REFERER).get //OrElse(fallbackURL)
      Redirect(redirectURL).withLang(Lang.apply(lang.code)).flashing(LanguageUtils.FlashWithSwitchIndicator)
  }

  private def isWelshEnabled: Boolean = appConfig.isWelshEnabled
}
