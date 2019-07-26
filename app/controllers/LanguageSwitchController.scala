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

  private def fallbackURL: String = routes.EiscdValidateController.index().url

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
