logLevel := Level.Warn

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.8")
addSbtPlugin("com.typesafe.sbt" % "sbt-play-ebean" % "3.0.0")
