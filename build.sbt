lazy val root = project
  .in(file("."))
  .settings(
    commonSettings,
    name := "investment-library"
  )

  val compiler2_13 = Seq(
  "-deprecation",
  "-encoding", "utf-8",
  "-explaintypes",
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint:inaccessible",
  "-Xlint:infer-any",
  "-Xlint:missing-interpolator",
  "-Xlint:option-implicit",
  "-Xlint:type-parameter-shadow",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused:implicits",
  "-Ywarn-unused:imports",
  "-Ywarn-unused:locals",
  "-Ywarn-unused:privates"
)

val commonSettings = Seq(
  organization  := "com.github.pheymann",
  version       := "0.0.0",
  scalaVersion  := "2.13.5",
  scalacOptions ++= compiler2_13,
  Global / cancelable := true,
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.2.0" % "test",
    "org.scalatest" %% "scalatest-funsuite" % "3.2.0" % "test",
    "org.scalacheck" %% "scalacheck" % "1.15.2" % "test"
  )
)
