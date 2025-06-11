import uk.gov.hmrc.DefaultBuildSettings

ThisBuild / scalaVersion := "2.13.16"
ThisBuild / majorVersion := 0

val appName = "bank-account-reputation-frontend"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    TwirlKeys.templateImports ++= Seq(
      "config.AppConfig",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.govukfrontend.views.html.components._"
    ),
    scalacOptions += "-Wconf:cat=unused-imports&src=html/.*:s",
    scalacOptions += "-Wconf:cat=unused-imports&src=routes/.*:s",
    Assets / pipelineStages := Seq(gzip),
  )
  .settings(PlayKeys.playDefaultPort := 9786)

lazy val it = project.in(file("it"))
  .enablePlugins(play.sbt.PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
