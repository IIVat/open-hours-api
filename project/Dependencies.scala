import sbt._

object Dependencies {
  lazy val akkaVersion = "2.6.8"
  lazy val akkaHttpVersion = "10.2.4"
  lazy val akkaHttpJsonVersion = "1.36.0"
  lazy val circeVersion = "0.14.1"
  lazy val scalatestVersion = "3.2.9"
  lazy val catsVersion = "2.3.0"

  lazy val akka = Seq(
    "com.typesafe.akka" %% "akka-actor-typed",
    "com.typesafe.akka" %% "akka-stream"
  ).map(_ % akkaVersion)

  lazy val circe = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % circeVersion)

  lazy val akkaHttp =
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion

  lazy val akkaHttpCirceJson =
    "de.heikoseeberger" %% "akka-http-circe" % akkaHttpJsonVersion

  lazy val cats =
    "org.typelevel" %% "cats-core" % catsVersion

  //Test
  lazy val scalatest = "org.scalatest" %% "scalatest" % scalatestVersion % Test
  lazy val akkaHttpTestKit = "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test
  lazy val akkaStreamTestKit = "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test

  lazy val dependencies =
    akka ++
      circe ++
      Seq(akkaHttp, akkaHttpCirceJson, cats) ++
      Seq(scalatest, akkaHttpTestKit, akkaStreamTestKit)
}
