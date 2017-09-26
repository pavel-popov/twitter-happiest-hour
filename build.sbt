name := "twitter-happiest-hour"

version := "1.0"

scalaVersion := "2.11.11"

val testDependencies = Seq(
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.scalatest" %% "scalatest" % "3.0.1"
)

libraryDependencies ++= Seq(
  "com.danielasfregola" %% "twitter4s" % "5.1",
  "com.twitter" %% "scalding-args" % "0.16.0",
  "org.apache.spark" %% "spark-sql" % "2.2.0",
  "org.json4s" %% "json4s-native" % "3.5.3"
) ++ testDependencies.map(_ % "test")

