
package object models {

  import play.api.data.Forms._
  import play.api.data._
  import play.api.data.validation.Constraints._

  val accountForm: Form[AccountForm] = Form(
    mapping(
      "sortCode" -> text.verifying(pattern(regex = """\d{6}""".r, error = "bars.label.sortCodeInvalid", name = "")),
      "accountNumber" -> text.verifying(pattern("""\d+""".r, error = "bars.label.accountNumberInvalid", name = "")),
      "csrfToken" -> nonEmptyText
    )(AccountForm.apply)(AccountForm.unapply)
  )

  val sortCodeForm: Form[SortCodeForm] = Form(
    mapping(
      "sortCode" -> text.verifying(pattern(regex = """\d{6}""".r, error = "bars.label.sortCodeInvalid", name = "")),
      "csrfToken" -> nonEmptyText
    )(SortCodeForm.apply)(SortCodeForm.unapply)
  )

  val inputForm: Form[InputForm] = Form(
    mapping(
      "input" -> mapping(
        "account" -> mapping(
          "sortCode" -> text.verifying(pattern(regex = """\d{6}""".r, error = "bars.label.sortCodeInvalid", name = "")),
          "accountNumber" -> text.verifying(pattern("""\d+""".r, error = "bars.label.accountNumberInvalid", name = ""))
        )(Account.apply)(Account.unapply),
        "subject" -> mapping(
          "title" -> optional(text.verifying(pattern(regex = """\D+""".r, error = "bars.label.titleInvalid", name = ""))),
          "name" -> optional(text.verifying(pattern(regex = """\D+""".r, error = "bars.label.nameInvalid", name = ""))),
          "firstName" -> optional(text.verifying(pattern(regex = """\D+""".r, error = "bars.label.firstNameInvalid", name = ""))),
          "lastName" -> optional(text.verifying(pattern(regex = """\D+""".r, error = "bars.label.lastNameInvalid", name = ""))),
          "dob" -> optional(text.verifying(pattern(regex = """[\d\]+""".r, error = "bars.label.dobInvalid", name = ""))),
          "address" -> mapping(
            "lines" -> list(text.verifying(pattern(regex = """.+""".r, error = "bars.label.lineInvalid", name = ""))),
            "town" -> optional(text.verifying(pattern(regex = """\D+""".r, error = "bars.label.townInvalid", name = ""))),
            "postcode" -> optional(text.verifying(pattern(regex = """.+""".r, error = "bars.label.postcodeInvalid", name = "")))
          )(Address.apply)(Address.unapply)
        )(Subject.apply)(Subject.unapply)
      )(Input.apply)(Input.unapply),
      "csrfToken" -> nonEmptyText
    )(InputForm.apply)(InputForm.unapply)
  )

}