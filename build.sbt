val sprayVersion = "1.3.2"
val akkaVersion = "2.3.6"

name := "shorty-scala"

scalaVersion := "2.11.5"

resolvers += "spray repo" at "http://repo.spray.io"

libraryDependencies ++= Seq(
  "commons-validator" % "commons-validator" % "1.4.1",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "io.spray" %% "spray-routing" % sprayVersion
)

// Test dependencies
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "io.spray" %% "spray-testkit" % sprayVersion % "test"
)