import sbt.*

object AppDependencies {
  private val bootstrapPlayVersion = "8.6.0"

  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % "8.5.0"
  )

  val test = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapPlayVersion % Test,
    "org.jsoup" % "jsoup" % "1.17.2" % Test
  )
}
