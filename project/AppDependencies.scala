import sbt.*

object AppDependencies {

  private val bootstrapPlayVersion = "9.18.0"
  private val playFrontendHmrcVersion = "12.7.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % playFrontendHmrcVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %%  "bootstrap-test-play-30"    % bootstrapPlayVersion,
    "org.jsoup"   %   "jsoup"                     % "1.20.1"
  ).map(_ % Test)
}
