name := "flowupserver"

version := "1.0"

lazy val `flowupserver` = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq( javaJdbc ,  cache , javaWs ,
  "org.easytesting" % "fest-assert-core" % "2.0M10",
  "org.easytesting" % "fest-assert" % "1.4"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

mappings in Universal in packageBin += file("Dockerfile") -> "Dockerfile"
