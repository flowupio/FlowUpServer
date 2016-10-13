name := "flowupserver"

version := "1.0"

lazy val `flowupserver` = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq( javaJdbc ,  cache , javaWs,
  "org.projectlombok" % "lombok" % "1.16.10",
  "com.novocode" % "junit-interface" % "0.11" % "test",
  "org.mockito" % "mockito-all" % "1.10.19" % "test",
  "org.hamcrest" % "hamcrest-junit" % "2.0.0.0" % "test"
)

libraryDependencies += "com.feth" % "play-authenticate_2.11" % "0.7.1"

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

topLevelDirectory := None

mappings in Universal in packageBin += file("Dockerfile") -> "Dockerfile"

testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v")
