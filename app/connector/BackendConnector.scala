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

import config.AppConfig
import models.{AccountDetails, Assessment, EiscdEntry, Input, ValidationErrorResult, ValidationResult}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse}
import models.Implicits._

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class BackendConnector @Inject()(http: HttpClient, bars: AppConfig) {

  private val urlValidate = s"${bars.baseUrl}/validateBankDetails"
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

  def metadata(sortCode: String)(implicit hc: HeaderCarrier): Future[Option[EiscdEntry]] = {

    http.GET[HttpResponse](urlMetadata + sortCode)
        .map(r => r -> r.status)
        .map{
          case (response, 200) => response.json.validate[EiscdEntry].asOpt
          case (_,        _) => None
        }
  }

  def assess(account: Input)(implicit hc: HeaderCarrier): Future[Assessment] = {
    http.POST(urlAssess, account).map(response => response.json.validate[Assessment].get)
  }
}

