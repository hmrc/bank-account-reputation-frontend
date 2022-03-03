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

package connector

import play.api.libs.json.{Json, Reads, Writes}

trait BarsAssessResponse

case class BarsAssessBadRequestResponse(code: String, desc: String) extends BarsAssessResponse

case class BarsAssessErrorResponse() extends BarsAssessResponse

case class BarsAssessSuccessResponse(accountNumberIsWellFormatted: ReputationResponseEnum,
                                             sortCodeIsPresentOnEISCD: ReputationResponseEnum,
                                             sortCodeBankName: Option[String],
                                             accountExists: ReputationResponseEnum,
                                             nameMatches: ReputationResponseEnum,
                                             sortCodeSupportsDirectDebit: ReputationResponseEnum,
                                             sortCodeSupportsDirectCredit: ReputationResponseEnum,
                                             nonStandardAccountDetailsRequiredForBacs: Option[ReputationResponseEnum],
                                             iban: Option[String]) extends BarsAssessResponse

object BarsAssessResponse {
  implicit val reads: Reads[BarsAssessSuccessResponse] = Json.reads[BarsAssessSuccessResponse]
  implicit val writes: Writes[BarsAssessSuccessResponse] = Json.writes[BarsAssessSuccessResponse]

  implicit val badrequestReads: Reads[BarsAssessBadRequestResponse] = Json.reads[BarsAssessBadRequestResponse]
}