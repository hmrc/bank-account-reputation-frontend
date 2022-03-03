/*
 * Copyright 2022 HM Revenue & Customs
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
import models.EiscdEntry
import models.Implicits._
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpException, HttpReads, HttpResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class BankAccountReputationConnector @Inject()(httpClient: HttpClient, appConfig: AppConfig) {

  def metadata(sortCode: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[EiscdEntry]] = {
    import HttpReads.Implicits.readRaw

    httpClient.GET[HttpResponse](appConfig.barsMetadataUrl + sortCode)
      .map(r => r -> r.status)
      .map {
        case (response, 200) => response.json.validate[EiscdEntry].asOpt
        case (_, _) => None
      }
  }

  def assessPersonal(accountName: String, sortCode: String, accountNumber: String, address: Option[BarsAddress], callingClient: String)
                    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Try[BarsAssessResponse]] = {

    val request = BarsPersonalAssessRequest(
      BarsAccount(sortCode, accountNumber),
      BarsSubject(None, Some(accountName), None, None, None, address)
    )

    httpClient
      .POST[BarsPersonalAssessRequest, HttpResponse](
        url = appConfig.barsPersonalAssessUrl,
        body = request,
        headers = Seq("True-Calling-Client" -> callingClient)
      )
      .map {
        case httpResponse if httpResponse.status == 200 =>
          Json.fromJson[BarsAssessSuccessResponse](httpResponse.json) match {
            case JsSuccess(result, _) => Success(result)
            case JsError(errors) =>
              Failure(new HttpException("Could not parse Json success response from BARs", httpResponse.status))
          }
        case httpResponse if httpResponse.status == 400 =>
          Json.fromJson[BarsAssessBadRequestResponse](httpResponse.json) match {
            case JsSuccess(result, _) => Success(result)
            case JsError(errors) =>
              Failure(new HttpException("Could not parse Json bad request response from BARs", httpResponse.status))
          }
        case httpResponse => Failure(new HttpException(httpResponse.body, httpResponse.status))
      }
      .recoverWith {
        case t: Throwable => Future.successful(Failure(t))
      }
  }

  def assessBusiness(companyName: String, sortCode: String,
                     accountNumber: String, address: Option[BarsAddress], callingClient: String)
                    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Try[BarsAssessResponse]] = {

    val request = BarsBusinessAssessRequest(
      BarsAccount(sortCode = sortCode, accountNumber = accountNumber),
      Some(BarsBusiness(companyName = companyName, address)))

    httpClient
      .POST[BarsBusinessAssessRequest, HttpResponse](url = appConfig.barsBusinessAssessUrl, body = request,
        headers = Seq("True-Calling-Client" -> callingClient))
      .map {
        case httpResponse if httpResponse.status == 200 =>
          Json.fromJson[BarsAssessSuccessResponse](httpResponse.json) match {
            case JsSuccess(result, _) => Success(result)
            case JsError(errors) =>
              Failure(new HttpException("Could not parse Json response from BARs", httpResponse.status))
          }
        case httpResponse if httpResponse.status == 400 =>
          Json.fromJson[BarsAssessBadRequestResponse](httpResponse.json) match {
            case JsSuccess(result, _) => Success(result)
            case JsError(errors) =>
              Failure(new HttpException("Could not parse Json bad request response from BARs", httpResponse.status))
          }
        case httpResponse => Failure(new HttpException(httpResponse.body, httpResponse.status))
      }
      .recoverWith {
        case t: Throwable => Future.successful(Failure(t))
      }
  }
}