// Comment to get more information during initialization
logLevel := Level.Warn

javaOptions ++= Seq("-Djline.terminal=off")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

resolvers += Resolver.typesafeIvyRepo("releases")
resolvers += Resolver.url("mvnrepository", url("https://mvnrepository.com/artifact/"))

// Use the Play sbt plugin for Play projects
//addSbtPlugin("com.typesafe.play" %% "sbt-plugin" % "2.3.0")
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.9")

addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "0.99.5.1")

//addSbtPlugin("com.sksamuel.scoverage" % "sbt-scoverage" % "0.95.9")

//addSbtPlugin("com.sksamuel.scoverage" %% "sbt-coveralls" % "0.0.5")
