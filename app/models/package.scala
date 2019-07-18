
package object models {

  import play.api.data._
  import play.api.data.Forms._
  import play.api.data.validation.Constraints._

  val accountForm: Form[AccountForm] = Form(
    mapping(
      "sortCode" -> text.verifying(pattern("""[0-9]{6}""".r, error = "A valid sort code is required")),
      "accountNumber" -> text.verifying(pattern("""[0-9.+]+""".r, error = "A valid account number is required")),
      "csrfToken" -> text
    )(AccountForm.apply)(AccountForm.unapply)
  )

}