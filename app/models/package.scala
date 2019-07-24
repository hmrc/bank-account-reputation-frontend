
package object models {

  import play.api.data._
  import play.api.data.Forms._
  import play.api.data.validation.Constraints._

  val accountForm: Form[AccountForm] = Form(
    mapping(
      "sortCode" -> text.verifying(pattern(regex = """\d{6}""".r, error = "bars.label.sortCodeInvalid", name = "")),
      "accountNumber" -> text.verifying(pattern("""\d+""".r, error = "bars.label.accountNumberInvalid", name = "")),
      "csrfToken" -> nonEmptyText
    )(AccountForm.apply)(AccountForm.unapply)
  )

}