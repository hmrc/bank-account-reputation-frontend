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

package config

import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject()(val config: Configuration, servicesConfig: ServicesConfig, val env: Environment) {
  val appName: String = config.get[String]("appName")

  val baseUrl = servicesConfig.baseUrl("bars")
  val barsPersonalAssessUrl = s"$baseUrl/verify/personal"
  val barsBusinessAssessUrl = s"$baseUrl/verify/business"
  val barsMetadataUrl = s"$baseUrl/metadata/"
  val isNonProduction = config.getOptional[Boolean]("microservice.non-production").getOrElse(true)

  val authGuideUrl = config.get[String]("authentication-guide-url")
  val AUTH_GUIDE_URL_PLACEHOLDER = "__AUTH_GUIDE_URL__"

  val footerLinkItems: Seq[String] = config.getOptional[Seq[String]]("footerLinkItems").getOrElse(Seq())

  lazy val analyticsToken: String = config.get[String]("google-analytics.token")
  lazy val analyticsHost: String = config.get[String]("google-analytics.host")

  lazy val isLocal: Boolean = config.get[Boolean]("microservice.services.stride-auth-local")

  def isAssessmentEnabled: Boolean = config.get[Boolean]("microservice.services.features.assessment")

  def isStrideAuthEnabled: Boolean = config.get[Boolean]("microservice.services.features.stride-auth-enabled")

}

object AppConfig {
  val srsRoleName = "bars_front_end_tool_user"
}