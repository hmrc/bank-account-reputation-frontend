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

package models

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class EiscdEntryTest extends AnyWordSpec with Matchers {

  import TransactionType._

  "EiscdEntryTest" when {
    val address = EiscdAddress(Seq("1 High Street"), Some("some-town"), None, Some(EiscdCountry("UK")), Some("EC1 " +
      "2BD"), None)

    "show bacs transaction support if bacs office status is M or N" should {
      val eiscdEntry = EiscdEntry("bank-code", "bank-name", address, None, BacsStatus.M, ChapsStatus.D, None, Seq(CR))

      "indicate dd support and no dr support when CR is disallowed" in {
        eiscdEntry.isDirectDebitSupported shouldBe true
        eiscdEntry.isDirectCreditSupported shouldBe false
      }

      "indicate no dd support but cr support when AU is diassallowed" in {
        val newEiscdEntry = eiscdEntry.copy(disallowedTransactions = Seq(AU))
        newEiscdEntry.isDirectDebitSupported shouldBe false
        newEiscdEntry.isDirectCreditSupported shouldBe true
      }

      "indicate no dd support and cr support when DR is diassallowed" in {
        val newEiscdEntry = eiscdEntry.copy(disallowedTransactions = Seq(DR))
        newEiscdEntry.isDirectDebitSupported shouldBe false
        newEiscdEntry.isDirectCreditSupported shouldBe true
      }
    }

    "show no bacs transaction support if bacs office status is NOT M or N" should {
      val eiscdEntry = EiscdEntry("bank-code", "bank-name", address, None, BacsStatus.NA, ChapsStatus.D, None, Seq(CR))

      "indicate dd support and no dr support when CR is disallowed" in {
        eiscdEntry.isDirectDebitSupported shouldBe false
        eiscdEntry.isDirectCreditSupported shouldBe false
      }

      "indicate no dd support but cr support when AU is diassallowed" in {
        val newEiscdEntry = eiscdEntry.copy(disallowedTransactions = Seq(AU))
        newEiscdEntry.isDirectDebitSupported shouldBe false
        newEiscdEntry.isDirectCreditSupported shouldBe false
      }

      "indicate no dd support and cr support when DR is diassallowed" in {
        val newEiscdEntry = eiscdEntry.copy(disallowedTransactions = Seq(DR))
        newEiscdEntry.isDirectDebitSupported shouldBe false
        newEiscdEntry.isDirectCreditSupported shouldBe false
      }
    }
  }
}
