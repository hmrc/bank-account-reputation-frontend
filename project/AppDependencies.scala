import sbt.*

object AppDependencies {

  private val bootstrapPlayVersion = "9.19.0"
  private val playFrontendHmrcVersion = "12.8.0"
  private val playSuffix = "-play-30"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% s"bootstrap-frontend$playSuffix" % bootstrapPlayVersion,
    "uk.gov.hmrc" %% s"play-frontend-hmrc$playSuffix" % playFrontendHmrcVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %%  s"bootstrap-test$playSuffix"    % bootstrapPlayVersion,
    "org.jsoup"   %   "jsoup"                         % "1.21.1"
  ).map(_ % Test)
}
