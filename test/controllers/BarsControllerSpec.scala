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

package controllers

import akka.stream.Materializer
import com.codahale.metrics.SharedMetricRegistries
import connector.ReputationResponseEnum.{Partial, Yes}
import connector.{BankAccountReputationConnector, BarsAssessSuccessResponse}
import models.{BacsStatus, ChapsStatus, EiscdAddress, EiscdEntry}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, _}

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.Success

class BarsControllerSpec extends AnyWordSpec with GuiceOneAppPerSuite with Matchers with MockitoSugar {
  implicit val ec = ExecutionContext.global
  implicit val timeout: FiniteDuration = 1 second

  val mockConnector: BankAccountReputationConnector = mock[BankAccountReputationConnector]

  val address: EiscdAddress = EiscdAddress(Seq("line1"), None, None, None, None, None)
  val eiscdEntry: Option[EiscdEntry] = Some(EiscdEntry("HSBC", "HBSC", address, Some("12121"), BacsStatus.M, ChapsStatus.I, Some("London"), bicBankCode = Some("HBUK")))

  val barsAssessResponse: Success[BarsAssessSuccessResponse] = Success(BarsAssessSuccessResponse(Yes, Yes, Some("HSBC"), Yes, Partial, Yes, Yes, None, Some("iban"), Some("partial-name")))

  when(mockConnector.metadata(any())(any(), any())).thenReturn(Future.successful(eiscdEntry))
  when(mockConnector.assessBusiness(any(), any(), any(), any(), any())(any(), any())).thenReturn(Future.successful(barsAssessResponse))
  when(mockConnector.assessPersonal(any(), any(), any(), any(), any())(any(), any())).thenReturn(Future.successful(barsAssessResponse))

  override implicit lazy val app: Application = {
    SharedMetricRegistries.clear()

    new GuiceApplicationBuilder()
      .overrides(bind[BankAccountReputationConnector].toInstance(mockConnector))
      .build()
  }

  private val injector = app.injector
  private val controller = injector.instanceOf[BarsController]

  implicit lazy val materializer: Materializer = app.materializer

  "BarsController" when {
    "verifying account details" should {
      "show errors when no data is passed in" in {
        val request = FakeRequest().withFormUrlEncodedBody().withCSRFToken

        val result = controller.postVerify().apply(request)
        status(result) shouldBe BAD_REQUEST

        contentAsString(result) should include("There is a problem")
        contentAsString(result) should include("input.account.sortCode-error")
      }

      "show an error when the sort code is not known to EISCD" in {
        when(mockConnector.metadata(meq("654321"))(any(), any())).thenReturn(Future.successful(None))

        val request = FakeRequest().withFormUrlEncodedBody("input.account.sortCode" -> "654321").withCSRFToken

        val result = controller.postVerify().apply(request)
        status(result) shouldBe BAD_REQUEST

        contentAsString(result) should include("There is a problem")
        contentAsString(result) should include("input.account.sortCode-error")
      }

      "perform a metadata request when a valid sort code is passed in on it's own" in {
        val request = FakeRequest().withMethod("POST").withFormUrlEncodedBody("input.account.sortCode" -> "123456").withCSRFToken

        val result = controller.postVerify().apply(request)
        status(result) shouldBe OK

        verify(mockConnector, times(1)).metadata(meq("123456"))(any(), any())
        verify(mockConnector, never).assessBusiness(any(), any(), any(), any(), any())(any(), any())
        verify(mockConnector, never).assessPersonal(any(), any(), any(), any(), any())(any(), any())

        contentAsString(result) should include("Sort code")
        contentAsString(result) should include("123456")

        contentAsString(result) should include("Bank code")
        contentAsString(result) should include("HSBC")

        contentAsString(result) should include("Bank identification code (BIC)")
        contentAsString(result) should include("HBUK")

        contentAsString(result) should include("Bank name")
        contentAsString(result) should include("HSBC")

        contentAsString(result) should include("Address")
        contentAsString(result) should include("line1")

        contentAsString(result) should include("Telephone")
        contentAsString(result) should include("12121")

        contentAsString(result) should include("BACS office status")
        contentAsString(result) should include("BACS member; accepts BACS payments")

        contentAsString(result) should include("CHAPS sterling status")
        contentAsString(result) should include("Indirect")

        contentAsString(result) should include("Branch name")
        contentAsString(result) should include("London")

        contentAsString(result) should include("Transaction types")
        contentAsString(result) should include("ALLOWED")
      }

      "show an error if account number is entered without a name" in {
        val request = FakeRequest().withMethod("POST").withFormUrlEncodedBody(
          "input.account.sortCode" -> "123456",
          "input.account.accountNumber" -> "12345678",
          "input.subject.name" -> "").withCSRFToken

        val result = controller.postVerify().apply(request)
        status(result) shouldBe BAD_REQUEST

        contentAsString(result) should include("There is a problem")
        contentAsString(result) should include("input.subject.name-error")
      }

      "show an error if account name is entered without a number" in {
        val request = FakeRequest().withMethod("POST").withFormUrlEncodedBody(
          "input.account.sortCode" -> "123456",
          "input.account.accountNumber" -> "",
          "input.subject.name" -> "Mr Peter Smith").withCSRFToken

        val result = controller.postVerify().apply(request)
        status(result) shouldBe BAD_REQUEST

        contentAsString(result) should include("There is a problem")
        contentAsString(result) should include("input.account.accountNumber-error")
      }

      "perform both a metadata request and a business assess request when account name and number are specified" in {
        clearInvocations(mockConnector)

        val request = FakeRequest().withMethod("POST").withFormUrlEncodedBody(
          "input.account.sortCode" -> "123456",
          "input.account.accountNumber" -> "12345678",
          "input.subject.name" -> "ACME inc").withCSRFToken

        val result = controller.postVerify().apply(request)
        status(result) shouldBe OK

        verify(mockConnector, times(1)).metadata(meq("123456"))(any(), any())
        verify(mockConnector, never).assessPersonal(any(), any(), any(), any(), any())(any(), any())
        verify(mockConnector, times(1)).assessBusiness(
          meq("ACME inc"),
          meq("123456"),
          meq("12345678"),
          meq(None),
          meq("bank-account-reputation-frontend"))(any(), any())

        contentAsString(result) should include("Account number")
        contentAsString(result) should include("12345678")

        contentAsString(result) should include("Sort code")
        contentAsString(result) should include("123456")

        contentAsString(result) should include("IBAN")
        contentAsString(result) should include("iban")

        contentAsString(result) should include("Account number is well formatted")
        contentAsString(result) should include("yes")

        contentAsString(result) should include("Non-standard account details required for BACS")
        contentAsString(result) should include("yes")

        contentAsString(result) should include("Account number exists")
        contentAsString(result) should include("yes")

        contentAsString(result) should include("Name matches")
        contentAsString(result) should include("partial")

        contentAsString(result) should include("Partially matched name")
        contentAsString(result) should include("partial-name")

        contentAsString(result) should include("Bank code")
        contentAsString(result) should include("HSBC")

        contentAsString(result) should include("Bank identification code (BIC)")
        contentAsString(result) should include("HBUK")

        contentAsString(result) should include("Bank name")
        contentAsString(result) should include("HSBC")

        contentAsString(result) should include("Address")
        contentAsString(result) should include("line1")

        contentAsString(result) should include("Telephone")
        contentAsString(result) should include("12121")

        contentAsString(result) should include("BACS office status")
        contentAsString(result) should include("BACS member; accepts BACS payments")

        contentAsString(result) should include("CHAPS sterling status")
        contentAsString(result) should include("Indirect")

        contentAsString(result) should include("Branch name")
        contentAsString(result) should include("London")

        contentAsString(result) should include("Transaction types")
        contentAsString(result) should include("ALLOWED")
      }

      "perform both a metadata request and a personal assess request when account name and number are specified" in {
        clearInvocations(mockConnector)

        val request = FakeRequest().withMethod("POST").withFormUrlEncodedBody(
          "input.account.sortCode" -> "123456",
          "input.account.accountNumber" -> "12345678",
          "input.subject.name" -> "Mr Peter Smith",
          "input.account.accountType" -> "personal").withCSRFToken

        val result = controller.postVerify().apply(request)
        status(result) shouldBe OK

        verify(mockConnector, never).assessBusiness(any(), any(), any(), any(), any())(any(), any())
        verify(mockConnector, times(1)).metadata(meq("123456"))(any(), any())
        verify(mockConnector, times(1)).assessPersonal(
          meq("Mr Peter Smith"),
          meq("123456"),
          meq("12345678"),
          meq(None),
          meq("bank-account-reputation-frontend"))(any(), any())

        contentAsString(result) should include("Account number")
        contentAsString(result) should include("12345678")

        contentAsString(result) should include("Sort code")
        contentAsString(result) should include("123456")

        contentAsString(result) should include("IBAN")
        contentAsString(result) should include("iban")

        contentAsString(result) should include("Account number is well formatted")
        contentAsString(result) should include("yes")

        contentAsString(result) should include("Non-standard account details required for BACS")
        contentAsString(result) should include("yes")

        contentAsString(result) should include("Account number exists")
        contentAsString(result) should include("yes")

        contentAsString(result) should include("Name matches")
        contentAsString(result) should include("yes")

        contentAsString(result) should include("Bank code")
        contentAsString(result) should include("HSBC")

        contentAsString(result) should include("Bank identification code (BIC)")
        contentAsString(result) should include("HBUK")

        contentAsString(result) should include("Bank name")
        contentAsString(result) should include("HSBC")

        contentAsString(result) should include("Address")
        contentAsString(result) should include("line1")

        contentAsString(result) should include("Telephone")
        contentAsString(result) should include("12121")

        contentAsString(result) should include("BACS office status")
        contentAsString(result) should include("BACS member; accepts BACS payments")

        contentAsString(result) should include("CHAPS sterling status")
        contentAsString(result) should include("Indirect")

        contentAsString(result) should include("Branch name")
        contentAsString(result) should include("London")

        contentAsString(result) should include("Transaction types")
        contentAsString(result) should include("ALLOWED")
      }
    }
  }
}
