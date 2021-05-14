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

import models.Implicits.{assessmentFormat, eiscdWrites, modcheckResultFormat, validationResultFormat}
import models._
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{times, verify}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import utils.TestData


class BackendConnectorSpec extends PlaySpec with MockitoSugar with ScalaFutures {
  implicit val hc = HeaderCarrier()

  "connector" should {

    "get metadata" in new TestData {
      mockGET(eiscdEntry)
      val response = connector.metadata(sortCode).futureValue

      response must be(eiscdEntry)
      verify(http, times(1)).GET(
        meq(s"http://localhost/metadata/$sortCode")
      )(any(), any(), any())
    }


    "validate" in new TestData {
      mockPOST(validateResult)
      val response = connector.validate(account).futureValue

      response must be(Right(validateResult))
      verify(http, times(1)).POST[AccountDetails, HttpResponse](
        meq("http://localhost/validateBankDetails"),
        meq(account),
        any()
      )(any(), any(), any(), any())
    }

    "validate with 400 response" in new TestData {
      import models.Implicits._

      mockPOST(errorValidateResult, 400)
      val response = connector.validate(hmrcAccount).futureValue

      response must be(Left(errorValidateResult))
      verify(http, times(1)).POST[AccountDetails, HttpResponse](
        meq("http://localhost/validateBankDetails"),
        meq(hmrcAccount),
        any()
      )(any(), any(), any(), any())
    }

    "modcheck" in new TestData {
      mockPOST(modCheckResult)
      val response = connector.modcheck(account).futureValue

      response must be(modCheckResult)
      verify(http, times(1)).POST[AccountDetails, HttpResponse](
        meq("http://localhost/modcheck"),
        meq(account),
        any()
      )(any(), any(), any(), any())
    }

    "assess" in new TestData {
      mockPOST(assessResult)
      val response = connector.assess(assessInput).futureValue

      response must be(assessResult)
      verify(http, times(1)).POST[Input, HttpResponse](
        meq("http://localhost/assess"),
        meq(assessInput),
        any()
      )(any(), any(), any(), any())
    }

  }

}
