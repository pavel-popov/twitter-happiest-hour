name := "twitter-happiest-hour"

version := "1.0"
scalaVersion := "2.11.11"

val testDependencies = Seq(
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.scalatest" %% "scalatest" % "3.0.1"
)

resolvers += Resolver.sonatypeRepo("releases")

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = true, includeDependency = true)

libraryDependencies ++= Seq(
  "com.danielasfregola" %% "twitter4s" % "5.1",
  "com.twitter" %% "scalding-args" % "0.16.0",
  "org.apache.spark" %% "spark-sql" % "2.2.0",
  "org.json4s" %% "json4s-native" % "3.5.3"
) ++ testDependencies.map(_ % "test")

import sbtassembly.MergeStrategy

val reverseConcat: MergeStrategy = new MergeStrategy {
  val name = "reverseConcat"
  def apply(tempDir: File, path: String, files: Seq[File]): Either[String, Seq[(File, String)]] =
    MergeStrategy.concat(tempDir, path, files.reverse)
}

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "services", "org.apache.hadoop.fs.FileSystem") => MergeStrategy.filterDistinctLines
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case PathList("application.conf") => MergeStrategy.discard
  case "BUILD" => MergeStrategy.discard
  case fileName if fileName.toLowerCase == "reference.conf" => reverseConcat
  case x => MergeStrategy.last
}

