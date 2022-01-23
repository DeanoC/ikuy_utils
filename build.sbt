//lazy val ikuy = ProjectRef(uri("git@github.com:DeanoC/ikuy2.git#master"),
// "root")
//lazy val ikuy = ProjectRef(file("../ikuy2"), "ikuy")
lazy val scala213               = "2.13.3"
lazy val scala212               = "2.12.10"
lazy val scala211               = "2.11.12"
lazy val supportedScalaVersions =
	List(scala213,
	     scala212,
	     scala211)


ThisBuild / scalaVersion := scala213
ThisBuild / organization := "com.deanoc"
ThisBuild / version := "1.0"
ThisBuild / scalacOptions += "-deprecation"
ThisBuild / licenses := Seq(
	"The MIT License (MIT)" -> url("http://opensource.org/licenses/MIT")
	)

ThisBuild / libraryDependencies := Seq(
	"tech.sparse" %% "toml-scala" % "0.2.2",
	"org.scala-lang.modules" %% "scala-collection-compat" % "2.3.1",
	"org.scala-lang" % "scala-reflect" % scalaVersion.value
	)
lazy val ikuy_utils = (project in file("."))
	.settings(crossScalaVersions := supportedScalaVersions)
