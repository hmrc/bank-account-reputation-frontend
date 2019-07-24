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
class EiscdValidateController @Inject()(
                                         connector: BackendConnector,
                                         mcc: MessagesControllerComponents,
                                         indexView: views.html.index,
                                         validateView: views.html.validate,
                                         validationResultView: views.html.validationResult
                                       )
                                       (implicit ec: ExecutionContext, appConfig: FrontendAppConfig) extends FrontendController(mcc) with I18nSupport {

  implicit val Hc: HeaderCarrier = HeaderCarrier()

  def index() = Action {

    implicit request =>

      Ok(indexView())
  }

  def metadataLookup = Action {

    implicit request =>

      Ok(views.html.metadata.render())
  }

  def metadata(sc: String) = Action {

    implicit request =>

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

  def validation = Action {

    implicit req =>

      Ok(validateView(accountForm))
  }

  def validate = Action {

    implicit request =>

      accountForm.bindFromRequest.fold(
        formWithErrors => {
          BadRequest(validateView(formWithErrors))
        },
        account => {
          //        call validation and return result
          Ok(validationResultView(account, ValidationResult(true, "", "", None, None, None, None))) //.flashing("success" -> "Bank details validated!")
        }
      )
  }
}