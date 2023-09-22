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

package utils

import config.AppConfig
import connector.BankAccountReputationConnector
import models._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar._
import play.api.libs.json.{Json, Writes}
import play.api.test.Helpers.OK
import uk.gov.hmrc.http.{HttpClient, HttpResponse}

import scala.concurrent.Future

@deprecated(message = "Not used anywhere?", since = "v0.0.1")
trait TestData {
  val config = mock[AppConfig]
  when(config.baseUrl).thenReturn("http://localhost")
  when(config.barsMetadataUrl).thenReturn(s"http://localhost/metadata/")

  val sortCode = "123456"
  val hmrcSortCode = "201147"
  val account: AccountDetails = AccountDetails(Account(sortCode, Some("12345678"), None))
  val hmrcAccount: AccountDetails = AccountDetails(Account(hmrcSortCode, Some("54697788"), None))
  val assessInput: Input = Input(account.account, Subject(Some("Mr James")))
  val http = mock[HttpClient]

  val connector = new BankAccountReputationConnector(http, config);

  val noEiscdEntry = None
  val yes = "Yes"

  val assessResult = Assessment(true, yes, yes, yes, yes, yes, None)

  def mockGET[T](data: T)(implicit writes: Writes[T]): Unit = {
    when(http.GET[HttpResponse](any(), any(), any())(any(), any(), any()))
      .thenReturn(Future.successful(HttpResponse(OK, Json.toJson(data).toString())))
  }

  def mockPOST[T](data: T, status: Int = OK)(implicit writes: Writes[T]): Unit = {
    when(http.POST[String, HttpResponse](any(), any(), any())(any(), any(), any(), any()))
      .thenReturn(Future.successful(HttpResponse(status, Json.toJson(data).toString())))
  }

  def mockPOSTException[T](exception: Exception): Unit = {
    when(http.POST[String, HttpResponse](any(), any(), any())(any(), any(), any(), any()))
      .thenReturn(Future.failed(exception))
  }

  def mockGETException[T](exception: Exception): Unit = {
    when(http.GET[HttpResponse](any())(any(), any(), any()))
      .thenReturn(Future.failed(exception))
  }

}
