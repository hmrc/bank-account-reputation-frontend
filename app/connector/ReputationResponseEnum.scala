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

package connector

import play.api.libs.json.{JsError, JsString, JsSuccess, Reads, Writes}

sealed trait ReputationResponseEnum

object ReputationResponseEnum extends Enumerable.Implicits {

  case object Yes extends WithName("yes") with ReputationResponseEnum
  case object Partial extends WithName("partial") with ReputationResponseEnum
  case object No extends WithName("no") with ReputationResponseEnum
  case object Indeterminate extends WithName("indeterminate") with ReputationResponseEnum
  case object Inapplicable extends WithName("inapplicable") with ReputationResponseEnum
  case object Error extends WithName("error") with ReputationResponseEnum

  val values: Seq[ReputationResponseEnum] = Seq(Yes, No, Partial, Indeterminate, Inapplicable, Error)

  implicit val enumerable: Enumerable[ReputationResponseEnum] =
    Enumerable(values.map(v => v.toString -> v): _*)
}

class WithName(string: String) {
  override val toString: String = string
}

trait Enumerable[A] {
  def withName(str: String): Option[A]
}

object Enumerable {
  def apply[A](entries: (String, A)*): Enumerable[A] =
    new Enumerable[A] {
      override def withName(str: String): Option[A] =
        entries.toMap.get(str)
    }

  trait Implicits {
    implicit def reads[A](implicit ev: Enumerable[A]): Reads[A] =
      Reads {
        case JsString(str) =>
          ev.withName(str)
            .map { s =>
              JsSuccess(s)
            }
            .getOrElse(JsError("error.invalid"))
        case _ =>
          JsError("error.invalid")
      }

    implicit def writes[A: Enumerable]: Writes[A] =
      Writes(value => JsString(value.toString))
  }
}
