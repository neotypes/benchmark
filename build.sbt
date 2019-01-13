scalaVersion := "2.11.8"

enablePlugins(JmhPlugin)

resolvers ++= Seq(
  "anormcypher" at "http://repo.anormcypher.org/",
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "com.dimafeng" %% "neotypes" % "0.4.0",
  "org.anormcypher" %% "anormcypher" % "0.10.0"
)