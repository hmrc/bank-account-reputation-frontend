package models

import play.api.libs.json.Json

case class AccountDetails(account: Account)

case class Account(sortCode: String, accountNumber: String)

case class ModCheckResult(accountNumberWithSortCodeIsValid: Boolean, nonStandardAccountDetailsRequiredForBacs: String)

case class ValidationResult(accountNumberWithSortCodeIsValid: Boolean,
                            nonStandardAccountDetailsRequiredForBacs: String,
                            sortCodeIsPresentOnEISCD: String,
                            supportsBACS: Option[String] = None,
                            ddiVoucherFlag: Option[String] = None,
                            directDebitsDisallowed: Option[String] = None,
                            directDebitInstructionsDisallowed: Option[String] = None
                           )

case class EiscdCountry(name: String)

case class EiscdAddress(lines: Seq[String],
                        town: Option[String],
                        county: Option[String],
                        country: Option[EiscdCountry],
                        postCode: Option[String],
                        zipCode: Option[String])

case class EiscdEntry(bankCode: String,
                      bankName: String,
                      address: EiscdAddress,
                      bacsOfficeStatus: String,
                      branchName: Option[String] = None,
                      ddiVoucherFlag: Option[String] = None
                      //                      ,                      disallowedTransactions: Seq[String] = Seq.empty
                     )

case class AccountForm(
                        sortCode: String,
                        accountNumber: String,
                        csrfToken: String
                      )

case class SortCodeForm(
                         sortCode: String,
                         csrfToken: String
                       )

object Implicits {

  implicit val optionStringFormat = play.api.libs.json.Format.optionWithNull[String]

  implicit val countryFormat = Json.format[EiscdCountry]

  implicit val addressFormat = Json.format[EiscdAddress]

  implicit val entryFormat = Json.format[EiscdEntry]

  implicit val accountFormat = Json.format[Account]

  implicit val accountDetailsFormat = Json.format[AccountDetails]

  implicit val validationResultFormat = Json.format[ValidationResult]

  implicit val modcheckResultFormat = Json.format[ModCheckResult]

  def opt(str: String): Option[String] = str.isEmpty match {
    case true => None
    case false => Some(str)
  }

}
