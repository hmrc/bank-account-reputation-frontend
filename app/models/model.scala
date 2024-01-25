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

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import models.BacsStatus.{M, A}
import models.ChapsStatus.ChapsStatus
import models.TransactionType.{AU, CR, DR, TransactionType}

case class AccountDetails(account: Account)

case class Account(sortCode: String,
                   accountNumber: Option[String],
                   accountType: Option[String] = None)

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

  val NA: Val = Val("!!! Unknown BACS Office Status !!!")

  val M: Val = Val("BACS member; accepts BACS payments")

  val A: Val = Val("Agency bank; accepts BACS payments")

  val N: Val = Val("Does not accept BACS payments")
}

object TransactionType extends Enumeration {
  type TransactionType = Value

  protected case class Val(name: String) extends super.Val

  import scala.language.implicitConversions

  implicit def valueToTransactionTypeVal(x: Value): Val = x.asInstanceOf[Val]

  val NA: Val = Val("!!! Unknown Transaction Type !!!")
  val DR: Val = Val("Direct Debits")
  val CR: Val = Val("BACS Credits")
  val CU: Val = Val("Claims for unpaid cheques")
  val PR: Val = Val("Life office debit")
  val BS: Val = Val("Building society credits")
  val DV: Val = Val("Dividend interest payments")
  val AU: Val = Val("AUDDIS")
}

object ChapsStatus extends Enumeration {

  type ChapsStatus = Value

  protected case class Val(status: String) extends super.Val

  import scala.language.implicitConversions

  implicit def valueToChapsStatusVal(x: Value): Val = x.asInstanceOf[Val]

  val NA: Val = Val("!!! Unknown CHAPS Sterling Status !!!")
  val D: Val = Val("Direct")
  val I: Val = Val("Indirect")
  val N: Val = Val("Does not accept")
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

case class Subject(name: Option[String])

case class Input(account: Account,
                 subject: Subject)

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

  implicit def bacsOfficeStatus(statusCode: String): BacsStatus.BacsStatus = BacsStatus.values.find(_.toString.matches(statusCode)).getOrElse(BacsStatus.NA)

  implicit def chapsSterlingStatus(statusCode: Option[String]): ChapsStatus = {
    statusCode match {
      case None    => ChapsStatus.NA
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
      )(EiscdEntry.apply _)

  implicit val eiscdWrites: Writes[EiscdEntry] = Json.writes[EiscdEntry]

  def opt(str: String): Option[String] = if (str.isEmpty) {None} else {Some(str)}
}
