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

import models._
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{times, verify}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestData

import scala.concurrent.ExecutionContext.Implicits.global


class BackendConnectorSpec extends PlaySpec with MockitoSugar with ScalaFutures {
  import models.Implicits._

  implicit val hc = HeaderCarrier()

  "connector" should {

    "get metadata" in new TestData {
      mockGET[Option[EiscdEntry]](eiscdEntry)
      val response = connector.metadata(sortCode).futureValue

      response must be(eiscdEntry)
      verify(http, times(1)).GET(
        meq(s"http://localhost/metadata/$sortCode"), any(), any()
      )(any(), any(), any())
    }
  }
}
