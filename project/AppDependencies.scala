import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-frontend-play-28" % "5.3.0",
    "uk.gov.hmrc"             %% "play-frontend-hmrc"         % "0.65.0-play-28"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % "5.3.0"             % "test, it",
    "org.scalatest"           %% "scalatest"                  % "3.1.4"             % "test, it",
    "org.jsoup"               %  "jsoup"                      % "1.13.1"            % "test, it",
    "com.typesafe.play"       %% "play-test"                  % PlayVersion.current % "test, it",
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.36.8"            % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.1.0"             % "test, it",
    "org.scalatestplus"       %% "mockito-3-4"                % "3.2.9.0"           % "test, it"
  )
}
