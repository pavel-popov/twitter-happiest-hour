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

assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs @ _*) => MergeStrategy.last
  case PathList("javax", "activation", xs @ _*) => MergeStrategy.last
  case PathList("org", "apache", xs @ _*) => MergeStrategy.last
  case PathList("com", "google", xs @ _*) => MergeStrategy.last
  case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.last
  case PathList("com", "codahale", xs @ _*) => MergeStrategy.last
  case PathList("com", "yammer", xs @ _*) => MergeStrategy.last
  case "about.html" => MergeStrategy.rename
  case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
  case "META-INF/mailcap" => MergeStrategy.last
  case "META-INF/mimetypes.default" => MergeStrategy.last
  case "plugin.properties" => MergeStrategy.last
  case "log4j.properties" => MergeStrategy.last
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
