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

package config

import controllers.routes
import javax.inject.Inject
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.Call
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class FrontendAppConfig @Inject()(config: ServicesConfig, val configuration: Configuration) {

  private def loadConfig(key: String) = config.getString(key)

  private lazy val contactHost = loadConfig("contact-frontend.host")
  private val contactFormServiceIdentifier = "childcarecalculatorfrontend"

  lazy val eligibilityUrl: String = config.baseUrl("cc-eligibility") + loadConfig("microservice.services.cc-eligibility.url")

  lazy val analyticsToken: String = loadConfig("google-analytics.token")
  lazy val analyticsHost: String = loadConfig("google-analytics.host")
  lazy val analyticsDimensionKey: String = loadConfig("google-analytics.dimensionKey")
  lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  lazy val betaFeedbackUrl = s"$contactHost/contact/beta-feedback"
  lazy val betaFeedbackUnauthenticatedUrl = s"$contactHost/contact/beta-feedback-unauthenticated"
  lazy val surveyUrl: String = loadConfig("feedback-survey-frontend.host")
  lazy val surveyThankYouUrl: String = loadConfig("feedback-survey-frontend.thankYou")

  lazy val authUrl: String = config.baseUrl("auth")
  lazy val loginUrl: String = loadConfig("urls.login")
  lazy val loginContinueUrl: String = loadConfig("urls.loginContinue")

  lazy val languageTranslationEnabled: Boolean = config.getBoolean("microservice.services.features.welsh-translation")

  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  def routeToSwitchLanguage: String => Call = (lang: String) => routes.LanguageSwitchController.switchToLanguage(lang)

  def isWelshEnabled: Boolean = config.getBoolean("microservice.services.features.welsh-translation")

  def isAssessmentEnabled: Boolean = config.getBoolean("microservice.services.features.assessment")

  lazy val minWorkingHours: Double = config.getString("workingHours.min").toDouble

  lazy val maxWorkingHours: Double = config.getString("workingHours.max").toDouble

  lazy val maxIncome: Double = config.getString("income.max").toDouble

  lazy val minIncome: Double = config.getString("income.min").toDouble

  lazy val maxEmploymentIncome: Double = config.getString("employmentIncome.max").toDouble

  lazy val minEmploymentIncome: Double = config.getString("employmentIncome.min").toDouble

  lazy val minNoWeeksStatPay: Int = config.getInt("noWeeksStatPay.min")

  lazy val maxNoWeeksMaternityPay: Int = config.getInt("noWeeksStatPay.maternity")

  lazy val maxNoWeeksPaternityPay: Int = config.getInt("noWeeksStatPay.paternity")

  lazy val maxNoWeeksAdoptionPay: Int = config.getInt("noWeeksStatPay.adoption")

  lazy val maxNoWeeksSharedParentalPay: Int = config.getInt("noWeeksStatPay.sharedParental")

  lazy val maxAmountChildren: Int = config.getInt("amountChildren.max")

  lazy val minAmountChildren: Int = config.getInt("amountChildren.min")

  lazy val navigationAudit: Boolean = config.getBoolean("feature.navigationAudit")
}
