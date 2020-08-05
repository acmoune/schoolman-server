name := """schoolman-server"""
organization := "com.epoqee"

version := "1.0.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.8"

SassKeys.generateSourceMaps := true

libraryDependencies ++= Seq(
  guice,
  "org.webjars" % "jquery" % "3.4.1",
  "org.webjars" % "bootstrap" % "3.4.1",
  "org.webjars" % "font-awesome" % "4.7.0",

  "com.typesafe.play" %% "play-slick" % "4.0.2",
  "com.typesafe.play" %% "play-slick-evolutions" % "4.0.2",
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",

  "org.mindrot" % "jbcrypt" % "0.3m",
  "com.auth0" % "java-jwt" % "3.8.3",

  "com.amazonaws" % "aws-java-sdk" % "1.11.696",

  "com.typesafe.play" %% "play-mailer" % "7.0.1",
  "com.typesafe.play" %% "play-mailer-guice" % "7.0.1",

  "com.hhandoko" %% "play2-scala-pdf" % "3.0.0.P26"
)
