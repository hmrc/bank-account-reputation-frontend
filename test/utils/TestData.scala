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

package utils

import config.BackendAppConfig
import connector.BackendConnector
import models._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.{Json, Writes}
import play.api.test.Helpers.OK
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.Future

trait TestData {
  val config = mock[BackendAppConfig]
  when(config.baseUrl).thenReturn("http://localhost")

  val sortCode = "123456"
  val hmrcSortCode = "201147"
  val account: AccountDetails = AccountDetails(Account(sortCode, "12345678"))
  val hmrcAccount: AccountDetails = AccountDetails(Account(hmrcSortCode, "54697788"))
  val assessInput: Input = Input(account.account, Subject(Some("Mr"), Some("James"), None, None, None, Address(List("line1"), None, None)))
  val http = mock[HttpClient]

  val connector = new BackendConnector(http, config);
  val address = EiscdAddress(Seq("line1"), None, None, None, None, None)
  val eiscdEntry = EiscdEntry("HSBC", "HBSC", address, Some("12121"), BacsStatus.M, ChapsStatus.I, Some("London"), bicBankCode = Some("HBUK"))
  val yes = "Yes"
  val validateResult = ValidationResult(true, yes, yes, Some(yes), Some(yes), Some(yes), Some("GB42ABCD12345612345678"))
  val errorValidateResult = ValidationErrorResult("SORT_CODE_ON_DENY_LIST", hmrcSortCode + ": sort code is on deny list. This usually means that it is an HMRC sort code.")
  val modCheckResult = ModCheckResult(true, yes)
  val assessResult = Assessment(true, yes, yes, yes, yes, yes, None)

  def mockGET[T](data: T)(implicit writes: Writes[T]): Unit = {
    when(http.GET[HttpResponse](any())(any(), any(), any()))
      .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(data)))))
  }

  def mockPOST[T](data: T, status: Int = OK)(implicit writes: Writes[T]): Unit = {
    when(http.POST[String, HttpResponse](any(), any(), any())(any(), any(), any(), any()))
      .thenReturn(Future.successful(HttpResponse(status, Some(Json.toJson(data)))))
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
