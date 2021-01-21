import sbt.Keys._
import sbt.Tests.{Group, SubProcess}
import sbt._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import com.typesafe.sbt.web.Import._
import net.ground5hark.sbt.concat.Import._
import com.typesafe.sbt.uglify.Import._
import com.typesafe.sbt.digest.Import._
import play.sbt.routes.RoutesKeys

trait MicroService {

  import uk.gov.hmrc._
  import DefaultBuildSettings._
  import uk.gov.hmrc.SbtAutoBuildPlugin
  import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
  import uk.gov.hmrc.versioning.SbtGitVersioning
  import play.sbt.routes.RoutesKeys.routesGenerator
  import play.sbt.routes.RoutesKeys
  import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

  val appName: String

  lazy val appDependencies : Seq[ModuleID] = ???
  lazy val plugins : Seq[Plugins] = Seq.empty
  lazy val playSettings : Seq[Setting[_]] = Seq.empty

  lazy val scoverageSettings: Seq[Def.Setting[_]] = {

    import scoverage._

    Seq(
      ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*filters.*;.*handlers.*;.*components.*;.*models.*;" +
        ".*BuildInfo.*;.*javascript.*;.*FrontendAuditConnector.*;.*Routes.*;.*GuiceInjector;.*DataCacheConnector;" +
        ".*ControllerConfiguration;.*LanguageSwitchController;.*repositories.*",
      ScoverageKeys.coverageMinimum := 90,
      ScoverageKeys.coverageFailOnMinimum := true,
      ScoverageKeys.coverageHighlighting := true
    )
  }

  lazy val microservice = Project(appName, file("."))
    .enablePlugins(Seq(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin) ++ plugins : _*)
    .settings(majorVersion := 0)
    .settings(playSettings : _*)
    .settings(RoutesKeys.routesImport ++= Seq("models._"))
    .settings(scalaSettings: _*)
    .settings(publishingSettings: _*)
    .settings(defaultSettings(): _*)
    .settings(scoverageSettings: _*)
    .settings(
      scalacOptions ++= Seq("-feature"),
      libraryDependencies ++= appDependencies,
      retrieveManaged := true,
      evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
      resolvers ++= Seq(
        Resolver.bintrayRepo("hmrc", "releases"),
        Resolver.jcenterRepo
      )
    )
    .settings(
      // concatenate js
      Concat.groups := Seq(
        "javascripts/childcarecalculatorfrontend-app.js" -> group(Seq("javascripts/show-hide-content.js", "javascripts/childcarecalculatorfrontend.js", "javascripts/polyfill.js"))
      ),
      // prevent removal of unused code which generates warning errors due to use of third-party libs
      uglifyCompressOptions := Seq("unused=false", "dead_code=false"),
      pipelineStages := Seq(digest),
      // below line required to force asset pipeline to operate in dev rather than only prod
      pipelineStages in Assets := Seq(concat,uglify),
      // only compress files generated by concat
      includeFilter in uglify := GlobFilter("childcarecalculatorfrontend-*.js")
    )
}

private object TestPhases {

  def oneForkedJvmPerTest(tests: Seq[TestDefinition]) =
    tests map {
      test => new Group(test.name, Seq(test), SubProcess(ForkOptions(runJVMOptions = Seq("-Dtest.name=" + test.name))))
    }
}