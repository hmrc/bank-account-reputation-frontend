package controllers

import config.FrontendAppConfig
import connector.BackendConnector
import javax.inject._
import models._
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class BarsController @Inject()(
                                connector: BackendConnector,
                                mcc: MessagesControllerComponents,
                                indexView: views.html.index,
                                metadataView: views.html.metadata,
                                metadataResultView: views.html.metadataResult,
                                modckeckView: views.html.modcheck,
                                modckeckResultView: views.html.modcheckResult,
                                validateView: views.html.validate,
                                validationResultView: views.html.validationResult,
                                assessmentView: views.html.assess,
                                assessmentResultView: views.html.assessmentResult
                              )
                              (implicit ec: ExecutionContext, appConfig: FrontendAppConfig) extends FrontendController(mcc) with I18nSupport {

  implicit val Hc: HeaderCarrier = HeaderCarrier()

  def index() = Action {

    implicit request =>

      Ok(indexView())
  }

  def metadataLookup = Action {

    implicit request =>

      Ok(metadataView(sortCodeForm))
  }

  def metadata = Action.async {

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

  def modChecking = Action {

    implicit req =>

      Ok(modckeckView(accountForm))
  }

  def modCheck = Action.async {

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

  def validation = Action {

    implicit req =>

      Ok(validateView(accountForm))
  }

  def validate = Action.async {

    implicit request =>

      accountForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest(validateView(formWithErrors)))
        },
        account => {
          connector.validate(AccountDetails(Account(account.sortCode, account.accountNumber)))
            .map(result =>
              Ok(validationResultView(account, result))
            )
        }
      )
  }

  def assessment = Action {

    implicit req =>

      Ok(assessmentView(inputForm))
  }

  def assess = Action.async {

    implicit request =>

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