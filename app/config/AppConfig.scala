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

package config

import controllers.routes

import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Environment}
import play.api.mvc.Call
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject()(val config: Configuration, servicesConfig: ServicesConfig, val env: Environment) {
  val baseUrl = servicesConfig.baseUrl("bars")

  val footerLinkItems: Seq[String] = config.getOptional[Seq[String]]("footerLinkItems").getOrElse(Seq())

  lazy val analyticsToken: String = config.get[String]("google-analytics.token")
  lazy val analyticsHost: String = config.get[String]("google-analytics.host")

  lazy val isLocal: Boolean = config.get[Boolean]("microservice.services.stride-auth-local")

  def isAssessmentEnabled: Boolean = config.get[Boolean]("microservice.services.features.assessment")

  def isStrideAuthEnabled: Boolean = config.get[Boolean]("microservice.services.features.stride-auth-enabled")

}
