import play.core.PlayVersion
import play.sbt.PlayImport.*
import sbt.*

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "7.12.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "5.2.0-play-28",
    "org.pac4j" %% "play-pac4j" % "10.0.2",
    "org.pac4j" % "pac4j-oidc" % "4.5.3" excludeAll (ExclusionRule(organization = "com.fasterxml.jackson.core")),
    "org.apache.shiro" % "shiro-core" % "1.7.1"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % "7.12.0"            % "test, it",
    "org.scalatest"           %% "scalatest"                  % "3.1.4"             % "test, it",
    "org.jsoup"               %  "jsoup"                      % "1.13.1"            % "test, it",
    "com.typesafe.play"       %% "play-test"                  % PlayVersion.current % "test, it",
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.36.8"            % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.1.0"             % "test, it",
    "org.scalatestplus"       % "mockito-4-6_2.13"            % "3.2.14.0"          % "test, it",
  )
}
