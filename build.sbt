import sbt._
import sbt.Keys._
import play.Play.autoImport._
import PlayKeys._
import java.io.File
import scala.sys.process._

instrumentSettings

CoverallsPlugin.coverallsSettings

name := "UnitCover"

version := "0.1"

scalaVersion := "2.10.4"

parallelExecution in Global := true //disable parallel execution for all tasks the below configuration could be deleted but for documentation purpose they are still there

parallelExecution in Test := true

parallelExecution in ScoverageTest := true

Keys.fork in Test := true

val logger = ProcessLogger(
    (o: String) => println("out " + o),
    (e: String) => println("err " + e))

lazy val npm = taskKey[Unit]("npm install")

npm := scala.sys.process.Process( "npm" :: "install" :: Nil) ! logger

//(compile in Compile) <<= (compile in Compile) dependsOn (npm)

ScoverageKeys.highlighting := true

ScoverageKeys.minimumCoverage := 80

ScoverageKeys.failOnMinimumCoverage := true

ScoverageKeys.excludedPackages in ScoverageCompile := "controllers.javascript;controllers.ref;tools.imports;Routes;controllers.ReverseAssets;controllers.ReverseApplication;controllers.ReverseBuildController;controllers.ReverseTestCaseController;controllers.ReverseTestSuiteController;controllers.ReverseBadgeController;scala.slick.migration.api..*"

envVars := Map("aes_key" -> "16rdKQfqN3L4TY7YktgxBw==", "sparkasse_username"->"username", "sparkasse_password"->"password") // setted for EasyCryptSpec

javaOptions ++= Seq("-Xmx2G", "-Xms1G", "-XX:MaxPermSize=256M", "-XX:+CMSClassUnloadingEnabled"/*, "-verbose:gc", "-XX:+PrintGCDetails", "-XX:+PrintGCTimeStamps"*/, "-XX:+UseConcMarkSweepGC", "-XX:ReservedCodeCacheSize=128M")

lazy val stickMigrationApi = RootProject(uri("git://github.com/nafg/slick-migration-api.git"))

lazy val root = (project in file(".")).enablePlugins(PlayScala).dependsOn(stickMigrationApi)

libraryDependencies += ws

// test dependencies
libraryDependencies ++= Seq(
    "org.hsqldb" %  "hsqldb" % "[2,)",
    "co.freeside" % "betamax" % "1.1.2" % "test",
    "org.codehaus.groovy" % "groovy-all" % "1.8.8" % "test"
)

libraryDependencies ++= Seq(
    "com.typesafe.slick" %% "slick" % "2.0.2",
    "mysql" % "mysql-connector-java" % "5.1.18",
    "c3p0" % "c3p0" % "0.9.1.2"
)