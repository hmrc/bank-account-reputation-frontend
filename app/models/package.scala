/*
 * Copyright 2019 HM Revenue & Customs
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

package object models {

  import play.api.data.Forms._
  import play.api.data._
  import play.api.data.validation.Constraints._

  val accountForm: Form[AccountForm] = Form(
    mapping(
      "sortCode" -> text.verifying(pattern(regex = """\d{6}""".r, error = "bars.label.sortCodeInvalid", name = "")),
      "accountNumber" -> text.verifying(pattern("""^(|\d{8})$""".r, error = "bars.label.accountNumberInvalid", name = ""))
    )(AccountForm.apply)(AccountForm.unapply)
  )

  val sortCodeForm: Form[SortCodeForm] = Form(
    mapping(
      "sortCode" -> text.verifying(pattern(regex = """\d{6}""".r, error = "bars.label.sortCodeInvalid", name = ""))
    )(SortCodeForm.apply)(SortCodeForm.unapply)
  )

  val inputForm: Form[InputForm] = Form(
    mapping(
      "input" -> mapping(
        "account" -> mapping(
          "sortCode" -> nonEmptyText.verifying(pattern(regex = """[0-9]{6}""".r, error = "bars.label.sortCodeInvalid", name = "")),
          "accountNumber" -> nonEmptyText.verifying(pattern("""[0-9]{8}""".r, error = "bars.label.accountNumberInvalid", name = ""))
        )(Account.apply)(Account.unapply),
        "subject" -> mapping(
          "title" -> optional(text.verifying(pattern(regex = """\D+""".r, error = "bars.label.titleInvalid", name = ""))),
          "name" -> optional(text.verifying(pattern(regex = """\D+""".r, error = "bars.label.nameInvalid", name = ""))),
          "firstName" -> optional(text.verifying(pattern(regex = """\D+""".r, error = "bars.label.firstNameInvalid", name = ""))),
          "lastName" -> optional(text.verifying(pattern(regex = """\D+""".r, error = "bars.label.lastNameInvalid", name = ""))),
          "dob" -> optional(text.verifying(pattern(regex = """[12][0-9]{3}-[01][0-9]-[0-3][0-9]""".r, error = "bars.label.dobInvalid", name = ""))),
          "address" -> mapping(
            "lines" -> list(text.verifying(pattern(regex = """.+""".r, error = "bars.label.lineInvalid", name = ""))),
            "town" -> optional(text.verifying(pattern(regex = """\D+""".r, error = "bars.label.townInvalid", name = ""))),
            "postcode" -> optional(nonEmptyText.verifying(pattern(regex = """.+""".r, error = "bars.label.postcodeInvalid", name = "")))
          )(Address.apply)(Address.unapply)
        )(Subject.apply)(Subject.unapply)
          .verifying("bars.label.fullNameAndPartNames", subject =>
            (subject.firstName.isDefined && subject.name.isEmpty) ||
              (subject.lastName.isDefined && subject.name.isEmpty) ||
              (subject.name.isDefined && subject.firstName.isEmpty && subject.lastName.isEmpty))
          .verifying("bars.label.partNamesInvalid", subject =>
            subject.firstName.isDefined && subject.lastName.isDefined ||
              subject.firstName.isEmpty && subject.lastName.isEmpty)
      )(Input.apply)(Input.unapply)
    )(InputForm.apply)(InputForm.unapply)
  )

}
