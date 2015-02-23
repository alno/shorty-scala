val sprayVersion = "1.3.2"
val akkaVersion = "2.3.6"

name := "shorty-scala"

scalaVersion := "2.11.5"

resolvers += "spray repo" at "http://repo.spray.io"

libraryDependencies ++= Seq(
  "commons-validator" % "commons-validator" % "1.4.1",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "io.spray" %% "spray-routing" % sprayVersion,
  "io.spray" %% "spray-can" % sprayVersion, // HTTP server
  "org.postgresql" % "postgresql" % "9.2-1004-jdbc41",
  "com.lucidchart" %% "relate" % "1.7.1" // SQL interpolation
)

// Test dependencies
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "io.spray" %% "spray-testkit" % sprayVersion % "test",
  "org.eu.acolyte" %% "jdbc-scala" % "1.0.32" % "test" // Database mocking
)

mainClass := Some("shorty.Application")
