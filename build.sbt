import io.gatling.sbt.GatlingPlugin
import sbt.Keys._

name := "flowupserver"

version := "1.0"

lazy val `flowupserver` = (project in file(".")).enablePlugins(PlayJava, PlayEbean)
  .enablePlugins(GatlingPlugin)
  .configs(GatlingTest)
  .settings(inConfig(GatlingTest)(Defaults.testSettings): _*)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(javaJdbc, cache, javaWs,
  "org.projectlombok" % "lombok" % "1.16.10",
  "com.novocode" % "junit-interface" % "0.11" % "test",
  "org.mockito" % "mockito-all" % "1.10.19" % "test",
  "org.hamcrest" % "hamcrest-junit" % "2.0.0.0" % "test",
  "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.2.2" % "test",
  "io.gatling" % "gatling-test-framework" % "2.2.2" % "test",
  "com.github.tomakehurst" % "wiremock" % "2.3.1" % "test",
  "org.webjars" %% "webjars-play" % "2.5.0",
  "org.webjars" % "bootstrap" % "3.3.4",
  "com.adrianhurt" %% "play-bootstrap" % "1.1-P25-B3",
  "mysql" % "mysql-connector-java" % "5.1.40",
  "be.objectify" %% "deadbolt-java" % "2.5.0",
  "com.spotify" % "completable-futures" % "0.3.0",
  "io.airbrake" % "airbrake-java" % "2.2.8",
  "com.github.karelcemus" %% "play-redis" % "1.3.0-M1"
)

topLevelDirectory := None

unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"


mappings in Universal in packageBin += file("aws-config/Dockerfile") -> "Dockerfile"
mappings in Universal in packageBin += file("aws-config/Dockerrun.aws.json") -> "Dockerrun.aws.json"
mappings in Universal in packageBin += file("aws-config/newrelic.jar") -> "newrelic.jar"
mappings in Universal in packageBin += file("aws-config/newrelic.yml") -> "newrelic.yml"

testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v")

includeFilter in(Assets, LessKeys.less) := "*.less"

excludeFilter in(Assets, LessKeys.less) := "_*.less"

lazy val GatlingTest = config("gatling") extend (Test)
scalaSource in GatlingTest := baseDirectory.value / "/gatling/simulation"