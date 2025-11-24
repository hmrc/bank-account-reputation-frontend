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

import models.BacsStatusEnum.{A, M}
import models.TransactionTypeEnum.{AU, CR, DR}
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

// BacsStatus, TransactionType, ChapsStatus case classes are used in the EiscdEntry to produce the audit detail in the required shape.

case class BacsStatus(status: String)
enum BacsStatusEnum(val status: String):
  case NA extends BacsStatusEnum("!!! Unknown BACS Office Status !!!")
  case M extends BacsStatusEnum("BACS member; accepts BACS payments")
  case A extends BacsStatusEnum("Agency bank; accepts BACS payments")
  case N extends BacsStatusEnum("Does not accept BACS payments")
  implicit def toCaseClass: BacsStatus = BacsStatus(status)

object BacsStatusEnum:
  implicit val bacsStatusFormat: Format[BacsStatusEnum] = Format(
    Reads {
      case JsString(value) =>
        BacsStatusEnum.values.find(_.toString == value).map(JsSuccess(_)).getOrElse(JsError(s"Unknown BacsStatus $value"))
      case _ => JsError(s"Expected string for BacsStatus")
    },
    Writes(status => JsString(status.toString))
  )

  implicit def toCaseClass(arg: BacsStatusEnum): BacsStatus = BacsStatus(arg.status)

case class TransactionType(name: String)
enum TransactionTypeEnum(val name: String):
  case NA extends TransactionTypeEnum("!!! Unknown Transaction Type !!!")
  case DR extends TransactionTypeEnum("Direct Debits")
  case CR extends TransactionTypeEnum("BACS Credits")
  case CU extends TransactionTypeEnum("Claims for unpaid cheques")
  case PR extends TransactionTypeEnum("Life office debit")
  case BS extends TransactionTypeEnum("Building society credits")
  case DV extends TransactionTypeEnum("Dividend interest payments")
  case AU extends TransactionTypeEnum("AUDDIS")
  implicit def toCaseClass: TransactionType = TransactionType(name)

object TransactionTypeEnum:
  implicit val transactionTypeFormat: Format[TransactionTypeEnum] = Format(
    Reads {
      case JsString(value) =>
        TransactionTypeEnum.values.find(_.toString == value).map(JsSuccess(_)).getOrElse(JsError(s"Unknown TransactionType $value"))
      case _ => JsError(s"Expected string for TransactionType")
    },
    Writes(status => JsString(status.toString))
  )
  implicit def toCaseClass(arg: TransactionTypeEnum): TransactionType = TransactionType(arg.name)

case class ChapsStatus(status: String)
enum ChapsStatusEnum(val status: String):
  case NA extends ChapsStatusEnum("!!! Unknown CHAPS Sterling Status !!!")
  case D extends ChapsStatusEnum("Direct")
  case I extends ChapsStatusEnum("Indirect")
  case N extends ChapsStatusEnum("Does not accept")
  implicit def toCaseClass: ChapsStatus = ChapsStatus(status)

object ChapsStatusEnum:
  implicit val chapsStatusFormat: Format[ChapsStatusEnum] = Format(
    Reads {
      case JsString(value) =>
        ChapsStatusEnum.values.find(_.toString == value).map(JsSuccess(_)).getOrElse(JsError(s"Unknown ChapsStatus $value"))
      case _ => JsError(s"Expected string for ChapsStatus")
    },
    Writes(status => JsString(status.toString))
  )
  implicit def toCaseClass(arg: ChapsStatusEnum): ChapsStatus = ChapsStatus(arg.status)

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
    (bacsOfficeStatus == M.toCaseClass || bacsOfficeStatus == A.toCaseClass) &&
      !(disallowedTransactions.contains(DR.toCaseClass) || disallowedTransactions.contains(AU.toCaseClass))

  def isDirectCreditSupported: Boolean =
    (bacsOfficeStatus == M.toCaseClass || bacsOfficeStatus == A.toCaseClass) && !disallowedTransactions.contains(CR.toCaseClass)
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

  implicit val bacsStatusFormat: OFormat[BacsStatus] = Json.format[BacsStatus]

  implicit val transactionTypeFormat: OFormat[TransactionType] = Json.format[TransactionType]

  implicit val chapsStatusFormat: OFormat[ChapsStatus] = Json.format[ChapsStatus]

  implicit def bacsOfficeStatus(statusCode: String): BacsStatus = BacsStatusEnum.values.find(_.toString.matches(statusCode)).getOrElse(BacsStatusEnum.NA)

  implicit def chapsSterlingStatus(statusCode: Option[String]): ChapsStatus = {
    statusCode match {
      case None => ChapsStatusEnum.NA
      case Some(x) => ChapsStatusEnum.values.find(_.toString.matches(x)).getOrElse(ChapsStatusEnum.NA)
    }
  }

  implicit def transactionType(transactionTypeCode: String): TransactionType =
    TransactionTypeEnum.values.find(_.toString.matches(transactionTypeCode)).getOrElse(TransactionTypeEnum.NA)

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

  implicit val eiscdWrites: Writes[EiscdEntry] = Json.writes[EiscdEntry]

  def opt(str: String): Option[String] = if (str.isEmpty) None else Some(str)
}
