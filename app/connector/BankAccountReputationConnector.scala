/*
 * Copyright 2023 HM Revenue & Customs
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
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpException, HttpReads, HttpResponse, StringContextOps}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class BankAccountReputationConnector @Inject()(httpClient: HttpClientV2, appConfig: AppConfig) {

  def metadata(sortCode: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[EiscdEntry]] = {
    import HttpReads.Implicits.readRaw

    httpClient
      .get(url"${appConfig.barsMetadataUrl}$sortCode")
      .execute[HttpResponse]
      .map(r => r -> r.status)
      .map {
        case (response, 200) => response.json.validate[EiscdEntry] match {
          case JsSuccess(entry, _) => Some(entry)
          case JsError(errors) =>
            throw new HttpException(s"Could not parse Json response from BARs: $errors", response.status)
        }
        case (_, _) => None
      }
  }

  def assessPersonal(accountName: String, sortCode: String, accountNumber: String, rollNumber: Option[String],
                     address: Option[BarsAddress], callingClient: String)
                    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Try[BarsAssessResponse]] = {

    val request: BarsPersonalAssessRequest = BarsPersonalAssessRequest(
      BarsAccount(sortCode, accountNumber, rollNumber),
      BarsSubject(None, Some(accountName), None, None, None, address)
    )

    httpClient
      .post(url"${appConfig.barsPersonalAssessUrl}")
      .withBody(Json.toJson(request))
      .setHeader("True-Calling-Client" -> callingClient)
      .execute[HttpResponse]
      .map {
        case httpResponse if httpResponse.status == 200 =>
          Json.fromJson[BarsAssessSuccessResponse](httpResponse.json) match {
            case JsSuccess(result, _) => Success(result)
            case JsError(_) =>
              Failure(new HttpException("Could not parse Json success response from BARs", httpResponse.status))
          }
        case httpResponse if httpResponse.status == 400 =>
          Json.fromJson[BarsAssessBadRequestResponse](httpResponse.json) match {
            case JsSuccess(result, _) => Success(result)
            case JsError(_) =>
              Failure(new HttpException("Could not parse Json bad request response from BARs", httpResponse.status))
          }
        case httpResponse => Failure(new HttpException(httpResponse.body, httpResponse.status))
      }
      .recoverWith {
        case t: Throwable => Future.successful(Failure(t))
      }
  }

  def assessBusiness(companyName: String, sortCode: String, accountNumber: String, rollNumber: Option[String],
                     address: Option[BarsAddress], callingClient: String)
                    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Try[BarsAssessResponse]] = {

    val request: BarsBusinessAssessRequest = BarsBusinessAssessRequest(
      BarsAccount(sortCode, accountNumber, rollNumber),
      Some(BarsBusiness(companyName = companyName, address)))

    httpClient
      .post(url"${appConfig.barsBusinessAssessUrl}")
      .withBody(Json.toJson(request))
      .setHeader("True-Calling-Client" -> callingClient)
      .execute[HttpResponse]
      .map {
        case httpResponse if httpResponse.status == 200 =>
          Json.fromJson[BarsAssessSuccessResponse](httpResponse.json) match {
            case JsSuccess(result, _) => Success(result)
            case JsError(_) =>
              Failure(new HttpException("Could not parse Json response from BARs", httpResponse.status))
          }
        case httpResponse if httpResponse.status == 400 =>
          Json.fromJson[BarsAssessBadRequestResponse](httpResponse.json) match {
            case JsSuccess(result, _) => Success(result)
            case JsError(_) =>
              Failure(new HttpException("Could not parse Json bad request response from BARs", httpResponse.status))
          }
        case httpResponse => Failure(new HttpException(httpResponse.body, httpResponse.status))
      }
      .recoverWith {
        case t: Throwable => Future.successful(Failure(t))
      }
  }
}
