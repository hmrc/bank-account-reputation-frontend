/*
 * Copyright 2024 HM Revenue & Customs
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

package models

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.data.Form
import play.api.libs.json.Json

class InputFormValidationSpec extends AnyFlatSpec with Matchers {

  import Implicits._

  private def testValidInput(withName: Option[String]) = Input(
    account = Account(
      sortCode = "123456",
      accountNumber = Some("12345678")
    ),
    subject = Subject(
      name = withName
    )
  )

  it should "succeed with input with name having only alpha and space chars" in {
    val validated = inputForm.bind(Json.toJson(InputForm(testValidInput(Some("john smith")))))
    validated.hasErrors shouldBe false
  }
  it should "succeed with input with name having only alpha,space and numeric chars" in {
    val validated = inputForm.bind(Json.toJson(InputForm(testValidInput(Some("john smith 1")))))
    validated.hasErrors shouldBe false
  }
  it should "fail with input with empty name" in {
    val validated = inputForm.bind(Json.toJson(InputForm(testValidInput(Some("")))))
    validated.errors should have length 1
    println(validated.errors.head)
  }
}
