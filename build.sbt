import com.github.retronym.SbtOneJar._

name := "rimbot"

version := "0.1.2-SNAPSHOT"

scalaVersion := "2.11.7"

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

lazy val http4sversion = "0.10.0"

lazy val scalazVersion = "7.1.3"

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation", "-feature")

libraryDependencies ++= Seq(
"jline" % "jline" % "2.13",
"pircbot" % "pircbot" % "1.5.0",
"org.http4s" %% "http4s-dsl" % http4sversion,
"org.http4s" %% "http4s-blaze-server" % http4sversion,
"org.http4s" %% "http4s-blaze-client" % http4sversion,
"org.http4s" %% "http4s-argonaut" % http4sversion,
"io.argonaut" %% "argonaut" % "6.0.4",
"org.scalaz" %% "scalaz-core" % scalazVersion,
"org.scalaz" %% "scalaz-effect" % scalazVersion,
"org.scalaz" %% "scalaz-typelevel" % scalazVersion,
"org.scalacheck" %% "scalacheck" % "1.12.5" % "test",
"org.scalaz" %% "scalaz-scalacheck-binding" % scalazVersion % "test",
"org.specs2" %% "specs2-core" % "3.6.4" % "test"
)

scalacOptions in Test ++= Seq("-Yrangepos")

oneJarSettings

scalariformSettings