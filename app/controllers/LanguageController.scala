package controllers

import config.FrontendAppConfig
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import util.LanguageUtils

import scala.concurrent.ExecutionContext

@Singleton
class LanguageController @Inject()(
                                    mcc: MessagesControllerComponents
                                  )
                                  (implicit ec: ExecutionContext, appConfig: FrontendAppConfig) extends FrontendController(mcc) with I18nSupport {


  def switchToEnglish = switchToLang(LanguageUtils.English)

  def switchToWelsh = switchToLang(LanguageUtils.Welsh)

  private def switchToLang(lang: Lang) = Action { implicit request =>
    request.headers.get(REFERER) match {
      case Some(referrer) => Redirect(referrer).withLang(lang).flashing(LanguageUtils.FlashWithSwitchIndicator)
      case None => {
        Logger.warn("Unable to get the referrer, so sending them to the start.")
        Redirect(controllers.routes.EiscdValidateController.index()).withLang(lang)
      }
    }
  }
}