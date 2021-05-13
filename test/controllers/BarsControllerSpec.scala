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

package controllers

import akka.stream.Materializer
import config.FrontendAppConfig
import models.Implicits.{eiscdWrites, validationResultFormat}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.mvc.{MessagesControllerComponents, Request}
import play.api.test.CSRFTokenHelper._
import play.api.test.Helpers.{status, _}
import play.api.test.{FakeRequest, Injecting}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.NotFoundException
import utils.TestData

import scala.concurrent.ExecutionContext

class BarsControllerSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting with MockitoSugar with TestData {
  implicit val ec = ExecutionContext.global
  implicit val appConfig = inject[FrontendAppConfig]
  implicit lazy val materializer: Materializer = app.materializer

  class Scenario extends BarsController(connector, inject[AuthConnector], inject[MessagesControllerComponents],
    inject[views.html.index], inject[views.html.accessibility], inject[views.html.metadata], inject[views.html.metadataResult], inject[views.html.modcheck], inject[views.html.modcheckResult],
    inject[views.html.validate], inject[views.html.validationResult], inject[views.html.validationErrorResult], inject[views.html.assess], inject[views.html.assessmentResult], inject[views.html.error_template])

  implicit class CSRFFRequestHeader(request: Request[_]) {
    def withCSRFToken: Request[_] = addCSRFToken(request)
  }

  "BarsController" should {

    "show index" should {
      "page" in new Scenario {
        val request = FakeRequest().withCSRFToken
        val result = index()(request)

        status(result) mustEqual OK
        contentAsString(result) must include("Validate Bank Details")
        contentAsString(result) must include("Sort Code")
        contentAsString(result) must include("Account Number [Optional]")
        contentAsString(result) must include("Search")
      }
    }

    "show accessibility statement" should {
      "page" in new Scenario {
        val request = FakeRequest().withCSRFToken
        val result = accessibilityStatement()(request)

        status(result) mustEqual OK
        contentAsString(result) must include("Accessibility statement for Bank Account Reputation Service")
      }
    }

    "validate" should {
      "show error when sortcode and account are not passed" in new Scenario {
        val json = Json.parse("""{ "sortCode": "", "accountNumber": "" }""")
        val request = FakeRequest().withJsonBody(json).withCSRFToken
        val result = validate(request)

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("Please review the following errors:")
        contentAsString(result) must include("A valid sort code is required")
      }

      "show errors when invalid sortcode and account are empty" in new Scenario {
        val json = Json.parse("""{ "sortCode": "12345", "accountNumber": "1234567" }""")
        val request = FakeRequest().withJsonBody(json).withCSRFToken
        val result = validate(request)

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("Please review the following errors:")
        contentAsString(result) must include("A valid sort code is required")
        contentAsString(result) must include("Invalid account number: should be 8 digits")
      }

      "show errors when  sortcode is valid and account is invalid " in new Scenario {
        val json = Json.parse("""{ "sortCode": "123456", "accountNumber": "1234567" }""")
        val request = FakeRequest().withJsonBody(json).withCSRFToken
        val result = validate(request)

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include("Please review the following errors:")
        contentAsString(result) must include("Invalid account number: should be 8 digits")
      }

      "show error when sortcode and account does not exist" in new Scenario {
        mockPOSTException(new NotFoundException("Not Found"))
        mockGETException(new NotFoundException("Not Found"))
        val json = Json.parse(s"""{ "sortCode": "$sortCode", "accountNumber": "${account.account.accountNumber}" }""")
        val request = FakeRequest().withJsonBody(json).withCSRFToken
        val result = validate(request)

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include(s"Failed to retrieve bank details for sort code $sortCode")
      }

      "show only sortcode data" in new Scenario {
        mockPOST(validateResult)
        mockGET(eiscdEntry)
        val json = Json.parse(s"""{ "sortCode": "$sortCode", "accountNumber": "" }""")
        val request = FakeRequest().withJsonBody(json).withCSRFToken
        val result = validate(request)

        status(result) mustEqual OK

        contentAsString(result) must include("Validate Bank Details")
        contentAsString(result) must include("IBAN")
        contentAsString(result) must include("N/A")
        contentAsString(result) must include("Account Number")
        contentAsString(result) must include("N/A")
        contentAsString(result) must include("Account Number/Sort Code Valid:")
        contentAsString(result) must include("N/A")
        contentAsString(result) must include("Bank Code:")
        contentAsString(result) must include("HSBC")
        contentAsString(result) must include("BIC Bank Code:")
        contentAsString(result) must include("HBUK")
        contentAsString(result) must include("Bank Name:")
        contentAsString(result) must include("HSBC")
        contentAsString(result) must include("Address:")
        contentAsString(result) must include("line1")
        contentAsString(result) must include("Telephone:")
        contentAsString(result) must include("12121")
        contentAsString(result) must include("BACS Office Status:")
        contentAsString(result) must include("BACS member; accepts BACS payments")
        contentAsString(result) must include("CHAPS Sterling Status:")
        contentAsString(result) must include("Indirect")
        contentAsString(result) must include("Branch Name:")
        contentAsString(result) must include("London")
        contentAsString(result) must include("Non Standard Account Details Required For BACS (e.g. Roll Number):")
        contentAsString(result) must include("no")
        contentAsString(result) must include("Transaction Types:")
        contentAsString(result) must include("ALLOWED")
      }


      "show both sortcode and account data" in new Scenario {
        mockPOST(validateResult)
        mockGET(eiscdEntry)
        val json = Json.parse(s"""{ "sortCode": "$sortCode", "accountNumber": "${account.account.accountNumber}" }""")
        val request = FakeRequest().withJsonBody(json).withCSRFToken
        val result = validate(request)

        status(result) mustEqual OK
        contentAsString(result) must include("Validate Bank Details")
        contentAsString(result) must include("IBAN")
        contentAsString(result) must include("GB42ABCD12345612345678")
        contentAsString(result) must include("Account Number")
        contentAsString(result) must include("12345678")
        contentAsString(result) must include("Account Number/Sort Code Valid:")
        contentAsString(result) must include("true")
        contentAsString(result) must include("Bank Code:")
        contentAsString(result) must include("HSBC")
        contentAsString(result) must include("BIC Bank Code:")
        contentAsString(result) must include("HBUK")
        contentAsString(result) must include("Bank Name:")
        contentAsString(result) must include("HSBC")
        contentAsString(result) must include("Address:")
        contentAsString(result) must include("line1")
        contentAsString(result) must include("Telephone:")
        contentAsString(result) must include("12121")
        contentAsString(result) must include("BACS Office Status:")
        contentAsString(result) must include("BACS member; accepts BACS payments")
        contentAsString(result) must include("CHAPS Sterling Status:")
        contentAsString(result) must include("Indirect")
        contentAsString(result) must include("Branch Name:")
        contentAsString(result) must include("London")
        contentAsString(result) must include("Non Standard Account Details Required For BACS (e.g. Roll Number):")
        contentAsString(result) must include("no")
        contentAsString(result) must include("Transaction Types:")
        contentAsString(result) must include("ALLOWED")
      }

    }
  }
}
