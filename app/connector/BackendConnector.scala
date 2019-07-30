package connector

import config.BackendAppConfig
import javax.inject.Inject
import models.Implicits._
import models.{AccountDetails, EiscdEntry, ValidationResult}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class BackendConnector @Inject()(
                                  http: HttpClient,
                                  bars: BackendAppConfig) {

  private val urlValidate = s"${bars.baseUrl}/validateBankDetails"
  private val urlMetadata = s"${bars.baseUrl}/metadata/"

  def validate(account: AccountDetails)(implicit hc: HeaderCarrier): Future[ValidationResult] = {

    http.POST(urlValidate, account).map(response => response.json.validate[ValidationResult].get)
  }

  def metadata(sortCode: String)(implicit hc: HeaderCarrier): Future[EiscdEntry] = {

    http.GET(urlMetadata + sortCode).map(response => response.status match {
      case 200 => response.json.validate[EiscdEntry].get
    })
  }
}

