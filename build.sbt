import sbt._
import sbt.Keys._
import play.Play.autoImport._
import PlayKeys._

instrumentSettings

//CoverallsPlugin.coverallsSettings

name := "UnitCover"

version := "0.1"

scalaVersion := "2.10.4"

parallelExecution in Global := true

parallelExecution in Test := true

parallelExecution in ScoverageTest := true

Keys.fork in Test := true

watchSources := (watchSources.value --- baseDirectory.value / "public"     ** "*").get

playRunHooks <+= baseDirectory.map(base => NPMPlayRunHook())

ScoverageKeys.highlighting := true

ScoverageKeys.minimumCoverage := 80

ScoverageKeys.failOnMinimumCoverage := true

ScoverageKeys.excludedPackages in ScoverageCompile := "controllers.javascript;controllers.ref;tools.imports;Routes;controllers.ReverseAssets;controllers.ReverseApplication;controllers.ReverseBuildController;controllers.ReverseTestCaseController;controllers.ReverseTestSuiteController;controllers.ReverseBadgeController;scala.slick.migration.api..*"

javaOptions ++= Seq("-Xmx2G", "-Xms1G", "-XX:+CMSClassUnloadingEnabled"/*, "-verbose:gc", "-XX:+PrintGCDetails", "-XX:+PrintGCTimeStamps"*/, "-XX:+UseConcMarkSweepGC", "-XX:ReservedCodeCacheSize=128M")

// resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
resolvers += Resolver.typesafeIvyRepo("releases")

resolvers += "JCenter" at "http://jcenter.bintray.com/"

lazy val ironCachePlugin = ProjectRef(uri("git://github.com/pussinboots/iron-cache-plugin.git"),"iron-cache-plugin")

//lazy val stickMigrationApi = RootProject(uri("git://github.com/nafg/slick-migration-api.git"))

lazy val root = (project in file(".")).enablePlugins(PlayScala)/*.dependsOn(stickMigrationApi)*/.dependsOn(ironCachePlugin)

libraryDependencies ++= Seq(ws, cache)

// test dependencies
//embedded jetty dependencies
libraryDependencies ++= Seq(
  "org.eclipse.jetty.orbit" % "javax.servlet" % "2.5.0.v201103041518" % "test" artifacts Artifact("javax.servlet", "jar", "jar"),
  "org.eclipse.jetty.orbit" % "javax.security.auth.message" % "1.0.0.v201108011116" % "test" artifacts Artifact("javax.security.auth.message", "jar", "jar"),
  "org.eclipse.jetty.orbit" % "javax.mail.glassfish" % "1.4.1.v201005082020" % "test" artifacts Artifact("javax.mail.glassfish", "jar", "jar"),
  "org.eclipse.jetty.orbit" % "javax.activation" % "1.1.0.v201105071233" % "test" artifacts Artifact("javax.activation", "jar", "jar"),
  "org.eclipse.jetty.orbit" % "javax.annotation" % "1.1.0.v201108011116" % "test" artifacts Artifact("javax.annotation", "jar", "jar"),
  "org.eclipse.jetty.aggregate" % "jetty-all-server" % "7.6.3.v20120416" % "test"
)

libraryDependencies += "com.typesafe" %% "webdriver" % "1.0.0"


libraryDependencies ++= Seq(
    "org.hsqldb" %  "hsqldb" % "[2,)",
    "co.freeside" % "betamax" % "1.1.2" % "test",
    "org.codehaus.groovy" % "groovy-all" % "1.8.8" % "test"
)

libraryDependencies ++= Seq(
    "com.typesafe.slick" %% "slick" % "2.0.2",
    "mysql" % "mysql-connector-java" % "5.1.18",
    "c3p0" % "c3p0" % "0.9.1.2",
    "io.github.nafg" %% "slick-migration-api" % "0.1.1"
)
