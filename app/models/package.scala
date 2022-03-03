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

import play.api.data.format.Formatter
import play.api.data.validation.{Constraint, Invalid}

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

package object models {

  import play.api.data.Forms._
  import play.api.data._
  import play.api.data.validation.Constraints._

  val inputForm: Form[InputForm] = Form(
    mapping(
      "input" -> mapping(
        "account" -> mapping(
          "sortCode" -> text.verifying(pattern(regex = """[0-9]{6}""".r, error = "bars.label.sortCodeInvalid", name = "")),
          "accountNumber" -> mandatoryIfSpecified("input.subject.name", pattern("""[0-9]{8}""".r, error = "bars.label.accountNumberInvalid", name = "input.subject.accountNumber")),
          "accountType" -> optional(text)
        )(Account.apply)(Account.unapply),
        "subject" -> mapping(
          "name" -> mandatoryIfSpecified("input.account.accountNumber", pattern(regex = """\D+""".r, error = "bars.label.nameInvalid", name = "input.subject.name"))
        )(Subject.apply)(Subject.unapply)
      )(Input.apply)(Input.unapply)
    )(InputForm.apply)(InputForm.unapply)
  )

  def mandatoryIfSpecified(fieldName: String, constraint: Constraint[String]): FieldMapping[Option[String]] =
    Forms.of[Option[String]](formatter(fieldName, constraint))

  def formatter(fieldName: String, constraint: Constraint[String]): Formatter[Option[String]] = new Formatter[Option[String]] {
    def bind(key: String, data: Map[String, String]): Either[Seq[FormError],Option[String]] = {
      if (data.getOrElse(fieldName, "").nonEmpty) {
        constraint.apply(data(key)) match {
          case Invalid(errors) => Left(errors.map(e => FormError(key, e.message, Nil)))
          case _ => Right(data.get(key).collect { case x if x.trim.nonEmpty => x })
        }
      } else {
       Right(data.get(key).collect { case x if x.trim.nonEmpty => x })
      }
    }

    override def unbind(key: String, value: Option[String]): Map[String, String] = Map(key -> value.getOrElse(""))
  }

}
