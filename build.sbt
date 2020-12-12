//lazy val ikuy = ProjectRef(uri("git@github.com:DeanoC/ikuy2.git#master"), "root")
//lazy val ikuy = ProjectRef(file("../ikuy2"), "ikuy")


ThisBuild / scalaVersion := "2.13.3"
ThisBuild / organization := "com.deanoc"
ThisBuild / version := "1.0"
ThisBuild / scalacOptions += "-deprecation"
ThisBuild / licenses := Seq(
	"The MIT License (MIT)" -> url("http://opensource.org/licenses/MIT")
	)

ThisBuild / libraryDependencies := Seq(
	"tech.sparse"            %% "toml-scala"               % "0.2.2",
	)
lazy val ikuy_utils = (project in file("."))
