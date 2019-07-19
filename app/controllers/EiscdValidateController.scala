package controllers

import javax.inject._
import models._
import play.api.mvc._

@Singleton
class EiscdValidateController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  // return true/false depending on whether the given sort code is actually in the EISCD data
  def metadataLookup = Action {
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