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

package util

import org.joda.time.format.DateTimeFormat
import org.joda.time.{LocalDate, LocalDateTime}
import play.api.i18n.Lang
import play.api.mvc.{Flash, RequestHeader}

object LanguageUtils {

  private val SelectedLanguageCookieName = "PLAY_LANG"
  private val MonthNamesInWelsh = Seq("Ionawr", "Chwefror", "Mawrth", "Ebrill", "Mai", "Mehefin", "Gorffennaf", "Awst", "Medi", "Hydref", "Tachwedd", "Rhagfyr")
  val TimeFormat = DateTimeFormat.forPattern("h:mma")
  val WelshLanguage = Lang("cy")
  val EnglishLanguage = Lang("en")
  val DateFormat = "d MMMM yyyy"
  val SwitchIndicatorKey = "switching-language"
  val FlashWithSwitchIndicator = Flash(Map(SwitchIndicatorKey -> "true"))

  val English = Lang("en")
  val Welsh = Lang("cy")

  def getCurrentLang(rh: RequestHeader): Lang = {
    rh.cookies.get(SelectedLanguageCookieName).map { cookie =>
      if (cookie.value == Welsh.code) Welsh else English
    }.getOrElse(English)
  }

  def getDisplayDate(date: LocalDate)(implicit displayLanguage: Lang) = if (displayLanguage == WelshLanguage) getDateAsWelsh(date) else getDateAsEnglish(date)

  def getDisplayDateRange(startDate: LocalDate, endDate: LocalDate)(implicit displayLanguage: Lang) =
    if (displayLanguage == WelshLanguage) getDateRangeAsWelsh(startDate, endDate) else getDateRangeAsEnglish(startDate, endDate)

  /**
    * Please note that DateFormat doesn't support printing the months in Welsh.
    *
    * http://docs.oracle.com/javase/tutorial/i18n/locale/identify.html
    *
    * @param date
    * @return Example: "8 Ionawr 2014"
    */
  def getDateAsWelsh(date: LocalDate) = Seq(date.getDayOfMonth.toString, MonthNamesInWelsh(date.getMonthOfYear - 1), date.getYear.toString).mkString(" ")

  def getDateAsEnglish(date: LocalDate) = date.toString(DateFormat)

  def getDateRangeAsEnglish(startDate: LocalDate, endDate: LocalDate) = Seq(getDateAsEnglish(startDate), getDateAsEnglish(endDate)).mkString(" ")

  def getDateRangeAsWelsh(startDate: LocalDate, endDate: LocalDate) = Seq(getDateAsWelsh(startDate), getDateAsWelsh(endDate)).mkString(" ")

  /**
    *
    * @param dateTime
    * @return Example: "8 Ionawr 2014 10:02 am"
    */
  def getDateTimeAsWelsh(dateTime: LocalDateTime) = Seq(getDateAsWelsh(dateTime.toLocalDate), dateTime.toString(TimeFormat).toLowerCase).mkString(" ")

}
