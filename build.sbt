name := "flowupserver"

version := "1.0"

lazy val `flowupserver` = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq( javaJdbc ,  cache , javaWs,
  "org.projectlombok" % "lombok" % "1.16.10",
  "com.novocode" % "junit-interface" % "0.11" % "test",
  "org.mockito" % "mockito-all" % "1.10.19" % "test",
  "org.hamcrest" % "hamcrest-junit" % "2.0.0.0" % "test"
)

libraryDependencies += "org.webjars" %% "webjars-play" % "2.5.0"
libraryDependencies += "org.webjars" % "bootstrap" % "3.3.4"
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.40"
libraryDependencies += "org.elasticsearch" % "elasticsearch" % "2.3.5"

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

topLevelDirectory := None

mappings in Universal in packageBin += file("aws-config/Dockerfile") -> "Dockerfile"
mappings in Universal in packageBin += file("aws-config/Dockerrun.aws.json") -> "Dockerrun.aws.json"

testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v")
