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

package models

import models.BacsStatus.{A, BacsStatus, M}
import models.ChapsStatus.ChapsStatus
import models.TransactionType.{AU, CR, DR, TransactionType}
import play.api.libs.functional.syntax.*
import play.api.libs.json.*
import play.api.libs.json.Reads.*

import scala.language.implicitConversions

case class AccountDetails(account: Account)

case class Account(sortCode: String,
                   accountNumber: Option[String],
                   rollNumber: Option[String] = None,
                   accountType: Option[String] = None)

object Account {
  def unapply(account: Account): Option[(String, Option[String], Option[String], Option[String])] = Some((
    account.sortCode,
    account.accountNumber,
    account.rollNumber,
    account.accountType
  ))
}

case class EiscdCountry(name: String)

case class EiscdAddress(lines: Seq[String],
                        town: Option[String],
                        county: Option[String],
                        country: Option[EiscdCountry],
                        postCode: Option[String],
                        zipCode: Option[String])

//TODO These need updating to use Scala 3 enumeration. Using out-the-box Scala 3 enums breaks how the data event keys are structured.
object BacsStatus extends Enumeration {

  type BacsStatus = BacsVal

  case class BacsVal(status: String) extends super.Val

  import scala.language.implicitConversions

  implicit def valueToBacsStatusVal(x: Value): BacsVal = x.asInstanceOf[BacsVal]

  val NA = BacsVal("!!! Unknown BACS Office Status !!!")
  val M = BacsVal("BACS member; accepts BACS payments")
  val A = BacsVal("Agency bank; accepts BACS payments")
  val N = BacsVal("Does not accept BACS payments")
}

object TransactionType extends Enumeration {
  type TransactionType = TransactionVal
  case class TransactionVal(name: String) extends super.Val

  import scala.language.implicitConversions
  implicit def valueToTransactionTypeVal(x: Value): TransactionVal = x.asInstanceOf[TransactionVal]

  val NA = TransactionVal("!!! Unknown Transaction Type !!!")
  val DR = TransactionVal("Direct Debits")
  val CR = TransactionVal("BACS Credits")
  val CU = TransactionVal("Claims for unpaid cheques")
  val PR = TransactionVal("Life office debit")
  val BS = TransactionVal("Building society credits")
  val DV = TransactionVal("Dividend interest payments")
  val AU = TransactionVal("AUDDIS")
}

object ChapsStatus extends Enumeration {

  type ChapsStatus = Value
  protected case class ChapsVal(status: String) extends super.Val
  import scala.language.implicitConversions

  implicit def valueToChapsStatusVal(x: Value): ChapsVal = x.asInstanceOf[ChapsVal]

  val NA = ChapsVal("!!! Unknown CHAPS Sterling Status !!!")
  val D  = ChapsVal("Direct")
  val I  = ChapsVal("Indirect")
  val N  = ChapsVal("Does not accept")
}

case class EiscdEntry(bankCode: String,
                      bankName: String,
                      address: EiscdAddress,
                      telephone: Option[String],
                      bacsOfficeStatus: BacsStatus,
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

case class Subject(name: Option[String])

object Subject {
  def unapply(subject: Subject): Option[Option[String]] = Some(subject.name)
}

case class Input(account: Account, subject: Subject)

object Input {
  def unapply(input: Input): Option[(Account, Subject)] = Some((input.account, input.subject))
}

case class Assessment(accountNumberWithSortCodeIsValid: Boolean,
                      accountExists: String,
                      nameMatches: String,
                      addressMatches: String,
                      nonConsented: String,
                      subjectHasDeceased: String,
                      nonStandardAccountDetailsRequiredForBacs: Option[String] = None)

case class AccountForm(sortCode: String,
                       accountNumber: Option[String])

case class SortCodeForm(sortCode: String)

case class InputForm(input: Input)

object InputForm {
  def unapply(inputForm: InputForm): Option[Input] = Some(inputForm.input)
}

object Implicits {

  implicit val optionStringFormat: Format[Option[String]] = play.api.libs.json.Format.optionWithNull[String]

  implicit val accountFormat: OFormat[Account] = Json.format[Account]

  implicit val accountDetailsFormat: OFormat[AccountDetails] = Json.format[AccountDetails]

  implicit val eiscdCountryFormat: OFormat[EiscdCountry] = Json.format[EiscdCountry]

  implicit val eiscdAddressFormat: OFormat[EiscdAddress] = Json.format[EiscdAddress]

  implicit val addressFormat: OFormat[Address] = Json.format[Address]

  implicit val subjectFormat: OFormat[Subject] = Json.format[Subject]

  implicit val inputFormat: OFormat[Input] = Json.format[Input]

  implicit val assessmentFormat: OFormat[Assessment] = Json.format[Assessment]

  implicit val inputFormFormat: OFormat[InputForm] = Json.format[InputForm]

  implicit def bacsOfficeStatus(statusCode: String): BacsStatus = BacsStatus.values.find(_.toString.matches(statusCode)).getOrElse(BacsStatus.NA)

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
      (JsPath \ "bacsOfficeStatus").read[String].map(bacsOfficeStatus) and
      (JsPath \ "chapsSterlingStatus").readNullable[String].map(chapsSterlingStatus) and
      (JsPath \ "branchName").readNullable[String] and
      (JsPath \ "disallowedTransactions").readNullable[Seq[String]].map(transactionTypes) and
      (JsPath \ "bicBankCode").readNullable[String]
      ) (EiscdEntry.apply _)

//  implicit val eiscdWrites: Writes[EiscdEntry] = Json.writes[EiscdEntry]

  def opt(str: String): Option[String] = if (str.isEmpty) None else Some(str)
}
