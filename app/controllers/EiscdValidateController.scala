package controllers

import config.FrontendAppConfig
import connector.BackendConnector
import javax.inject._
import models._
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.ExecutionContext

@Singleton
class EiscdValidateController @Inject()(connector: BackendConnector,
                                        mcc: MessagesControllerComponents,
                                        indexView: views.html.index)
                                       (implicit ec: ExecutionContext, appConfig: FrontendAppConfig) extends FrontendController(mcc) with I18nSupport {

  implicit val Hc: HeaderCarrier = HeaderCarrier()

  def index() = Action { implicit request =>
    Ok(indexView())
  }

  // return true/false depending on whether the given sort code is actually in the EISCD data
  def metadataLookup = Action { implicit request =>
    Ok(views.html.metadata.render())
  }

  // return true/false depending on whether the given sort code is actually in the EISCD data
  def metadata(sc: String) = Action {
    try {
      sc.matches("") match {
        //        case Some(entry) => Ok(Json.toJson(entry))
        //        case true => Ok(Json.toJson(entry))
        case true => Ok("MetaData Data")
        case _ => NotFound
      }
    } catch {
      case e: IllegalStateException => InternalServerError
    }
  }

  def validation = Action { implicit req =>

    Ok(views.html.validate(accountForm))
  }

  def validate = Action { implicit request =>
    accountForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.validate(formWithErrors))
      },
      account => {
        //        call validation and return result
        Ok(views.html.validationResult(ValidationResult(true, "", "", None, None, None, None))) //.flashing("success" -> "Bank details validated!")
      }
    )
  }

  def update = Action {
    Ok("false")
  }

}