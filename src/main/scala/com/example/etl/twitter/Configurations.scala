package com.example.etl.twitter

import com.danielasfregola.twitter4s.util
import com.typesafe.config.ConfigFactory

object HappyTweetsJobConfig extends util.ConfigurationDetector {

  val config = ConfigFactory.load

  lazy val batchSize = config.getInt("HappyTweetsJob.batchSize")
  lazy val happyTweetsDir = config.getString("HappyTweetsJob.happyTweetsDir")
}

object HappiestHourJobConfig extends util.ConfigurationDetector {

  val config = ConfigFactory.load

  lazy val sparkMaster = config.getString("HappiestHourJob.spark.master")
  lazy val minHashTagsLength = config.getInt("HappiestHourJob.minHashTagsLength")
}
