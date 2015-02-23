configs(IntegrationTest)
enablePlugins(GatlingPlugin)

val sprayVersion = "1.3.2"
val akkaVersion = "2.3.6"

name := "shorty-scala"

scalaVersion := "2.11.5"

resolvers += "spray repo" at "http://repo.spray.io"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "io.spray" %% "spray-routing" % sprayVersion,
  "io.spray" %% "spray-can" % sprayVersion, // HTTP server
  "org.postgresql" % "postgresql" % "9.2-1004-jdbc41",
  "com.lucidchart" %% "relate" % "1.7.1" // SQL interpolation
)

libraryDependencies += ("commons-validator" % "commons-validator" % "1.4.1").
  exclude("commons-beanutils", "commons-beanutils-core").
  exclude("commons-collections", "commons-collections").
  exclude("commons-logging", "commons-logging")

// Test dependencies
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "io.spray" %% "spray-testkit" % sprayVersion % "test",
  "org.eu.acolyte" %% "jdbc-scala" % "1.0.32" % "test" // Database mocking
)

// Integration test dependencies
libraryDependencies ++= Seq(
  "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.1.3" % "it",
  "io.gatling"            % "gatling-test-framework"    % "2.1.3" % "it"
)

mainClass := Some("shorty.Application")

