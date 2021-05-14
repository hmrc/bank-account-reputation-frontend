/*
 * Copyright 2021 HM Revenue & Customs
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

package connector

import akka.http.scaladsl.model.StatusCodes.OK
import config.BackendAppConfig

import javax.inject.Inject
import models.Implicits._
import models._
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class BackendConnector @Inject()(
                                  http: HttpClient,
                                  bars: BackendAppConfig) {

  private val urlValidate = s"${bars.baseUrl}/validateBankDetails"
  private val urlModcheck = s"${bars.baseUrl}/modcheck"
  private val urlMetadata = s"${bars.baseUrl}/metadata/"
  private val urlAssess = s"${bars.baseUrl}/assess"

  implicit val sensibleReads: HttpReads[HttpResponse] = new HttpReads[HttpResponse] {
    def read(method: String, url: String, response: HttpResponse) = response
  }

  def validate(account: AccountDetails)(implicit hc: HeaderCarrier): Future[Either[ValidationErrorResult, ValidationResult]] = {

    http.POST[AccountDetails, HttpResponse](urlValidate, account).map{
      case response if response.status == 200 =>
        Right(response.json.validate[ValidationResult].get)
      case response if response.status == 400 =>
        Left(response.json.validate[ValidationErrorResult].get)
    }
  }

  def modcheck(account: AccountDetails)(implicit hc: HeaderCarrier): Future[ModCheckResult] = {

    http.POST(urlModcheck, account).map(response => response.json.validate[ModCheckResult].get)
  }

  def metadata(sortCode: String)(implicit hc: HeaderCarrier): Future[EiscdEntry] = {

    http.GET(urlMetadata + sortCode).map(response => response.json.validate[EiscdEntry].get)
  }

  def assess(account: Input)(implicit hc: HeaderCarrier): Future[Assessment] = {

    http.POST(urlAssess, account).map(response => response.json.validate[Assessment].get)
  }
}

