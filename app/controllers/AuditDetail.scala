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

package controllers

import scala.util.Try

object AuditDetail {

  def from(path: String, property: Any): Map[String, String] = {
      property match {
        case s: Seq[_] =>
          s.zipWithIndex.flatMap {
            case (e, i) => from(s"$path.${i + 1}", e)
          }.toMap
        case t: Try[_] =>
          t.toOption.map(v => from(path, v)).toSeq.flatten.toMap
        case o: Option[_] =>
          o.map(v => from(path, v)).toSeq.flatten.toMap
        case p: Product if p.productArity > 0 =>
          p.productIterator.zipWithIndex.toSeq.foldLeft(Map[String, String]()) {
            case (acc, (property, idx)) =>
              val prefix = s"$path.${p.productElementName(idx)}"
              acc ++ from(prefix, property)
          }
        case v =>
          Map(s"$path" -> v.toString)
      }
  }

  def fromList(prefix: String, list: Seq[String]): Map[String, String] = list.zipWithIndex.map(kv => prefix + (kv._2 + 1).toString -> kv._1).toMap

  def fromMap(prefix: String, m: Map[String, String]): Map[String, String] = prefixAllKeys(prefix, m)

  def fromMap(prefix: String, m: Option[Map[String, String]]): Map[String, String] = prefixAllKeys(prefix, m getOrElse Map())

  private def prefixAllKeys(prefix: String, m: Map[String, String]): Map[String, String] =
    if (prefix.isEmpty) m
    else m.map(kv => prefix + kv._1 -> kv._2)
}
