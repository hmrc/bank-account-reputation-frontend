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

package models

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import models.BacsStatus.{M, A}
import models.ChapsStatus.ChapsStatus
import models.TransactionType.{AU, CR, DR, TransactionType}

case class AccountDetails(account: Account)

case class Account(sortCode: String,
                   accountNumber: String)

case class EiscdCountry(name: String)

case class EiscdAddress(lines: Seq[String],
                        town: Option[String],
                        county: Option[String],
                        country: Option[EiscdCountry],
                        postCode: Option[String],
                        zipCode: Option[String])

object BacsStatus extends Enumeration {

  type BacsStatus = Value

  protected case class Val(status: String) extends super.Val

  import scala.language.implicitConversions

  implicit def valueToBacsStatusVal(x: Value): Val = x.asInstanceOf[Val]

  val NA = Val("!!! Unknown BACS Office Status !!!")

  val M = Val("BACS member; accepts BACS payments")

  val A = Val("Agency bank; accepts BACS payments")

  val N = Val("Does not accept BACS payments")
}

object TransactionType extends Enumeration {
  type TransactionType = Value
  protected case class Val(name: String) extends super.Val

  import scala.language.implicitConversions
  implicit def valueToTransactionTypeVal(x: Value): Val = x.asInstanceOf[Val]

  val NA = Val("!!! Unknown Transaction Type !!!")
  val DR = Val("Direct Debits")
  val CR = Val("BACS Credits")
  val CU = Val("Claims for unpaid cheques")
  val PR = Val("Life office debit")
  val BS = Val("Building society credits")
  val DV = Val("Dividend interest payments")
  val AU = Val("AUDDIS")
}

object ChapsStatus extends Enumeration {

  type ChapsStatus = Value
  protected case class Val(status: String) extends super.Val
  import scala.language.implicitConversions

  implicit def valueToChapsStatusVal(x: Value): Val = x.asInstanceOf[Val]

  val NA = Val("!!! Unknown CHAPS Sterling Status !!!")
  val D = Val("Direct")
  val I = Val("Indirect")
  val N = Val("Does not accept")
}

case class EiscdEntry(bankCode: String,
                      bankName: String,
                      address: EiscdAddress,
                      telephone: Option[String],
                      bacsOfficeStatus: BacsStatus.BacsStatus,
                      chapsSterlingStatus: ChapsStatus,
                      branchName: Option[String] = None,
                      disallowedTransactions: Seq[TransactionType] = Seq.empty,
                      bicBankCode: Option[String] = None) {
  def isDirectDebitSupported: Boolean =
    (bacsOfficeStatus == M || bacsOfficeStatus == A) &&
      !(disallowedTransactions.contains(DR) || disallowedTransactions.contains(AU))

  def isDirectCreditSupported: Boolean =
    (bacsOfficeStatus == M || bacsOfficeStatus == A) && !disallowedTransactions.contains(CR)
}

case class Address(lines: List[String],
                   town: Option[String],
                   postcode: Option[String])

case class Subject(title: Option[String],
                   name: Option[String],
                   firstName: Option[String],
                   lastName: Option[String],
                   dob: Option[String],
                   address: Address)

case class Input(account: Account,
                 subject: Subject)

case class ValidationResult(accountNumberWithSortCodeIsValid: Boolean,
                            nonStandardAccountDetailsRequiredForBacs: String,
                            sortCodeIsPresentOnEISCD: String,
                            supportsBACS: Option[String] = None,
                            directDebitsDisallowed: Option[String] = None,
                            directDebitInstructionsDisallowed: Option[String] = None,
                            iban: Option[String] = None)

case class ValidationErrorResult(code: String, desc: String)

case class Assessment(accountNumberWithSortCodeIsValid: Boolean,
                      accountExists: String,
                      nameMatches: String,
                      addressMatches: String,
                      nonConsented: String,
                      subjectHasDeceased: String,
                      nonStandardAccountDetailsRequiredForBacs: Option[String] = None)

case class AccountForm(sortCode: String,
                       accountNumber: String)

case class SortCodeForm(sortCode: String)

case class InputForm(input: Input)

object Implicits {

  implicit val optionStringFormat = play.api.libs.json.Format.optionWithNull[String]

  implicit val accountFormat = Json.format[Account]

  implicit val accountDetailsFormat = Json.format[AccountDetails]

  implicit val eiscdCountryFormat = Json.format[EiscdCountry]

  implicit val eiscdAddressFormat = Json.format[EiscdAddress]

  implicit val addressFormat = Json.format[Address]

  implicit val subjectFormat = Json.format[Subject]

  implicit val inputFormat = Json.format[Input]

  implicit val validationResultFormat = Json.format[ValidationResult]

  implicit val validationErrorResultFormat = Json.format[ValidationErrorResult]

  implicit val assessmentFormat = Json.format[Assessment]

  implicit def bacsOfficeStatus(statusCode: String): BacsStatus.BacsStatus = BacsStatus.values.find(_.toString.matches(statusCode)).getOrElse(BacsStatus.NA)

  implicit def chapsSterlingStatus(statusCode: Option[String]): ChapsStatus = {
    statusCode match {
      case None => ChapsStatus.NA
      case Some(x) => ChapsStatus.values.find(_.toString.matches(x)).getOrElse(ChapsStatus.NA)
    }
  }

  implicit def transactionType(transactionTypeCode: String): TransactionType = TransactionType.values.find(_.toString.matches(transactionTypeCode)).getOrElse(TransactionType.NA)

  implicit def transactionTypes(transactionTypeCodes: Option[Seq[String]]): Seq[TransactionType] = transactionTypeCodes.getOrElse(Seq.empty).map(code => transactionType(code))

  implicit val eiscdReads: Reads[EiscdEntry] =
    ((JsPath \ "bankCode").read[String] and
      (JsPath \ "bankName").read[String] and
      (JsPath \ "address").read[EiscdAddress] and
      (JsPath \ "telephone").readNullable[String] and
      (JsPath \ "bacsOfficeStatus").read[String].map(bacsOfficeStatus _) and
      (JsPath \ "chapsSterlingStatus").readNullable[String].map(chapsSterlingStatus _) and
      (JsPath \ "branchName").readNullable[String] and
      (JsPath \ "disallowedTransactions").readNullable[Seq[String]].map(transactionTypes _) and
      (JsPath \ "bicBankCode").readNullable[String]
      ) (EiscdEntry.apply _)

  implicit val eiscdWrites: Writes[EiscdEntry] = Json.writes[EiscdEntry]

  def opt(str: String): Option[String] = str.isEmpty match {
    case true => None
    case false => Some(str)
  }
}
