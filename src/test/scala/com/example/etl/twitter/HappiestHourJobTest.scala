package com.example.etl.twitter

import org.apache.spark.SparkConf
import org.scalatest._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.col
import org.joda.time.DateTime

class HappiestHourJobTest extends FlatSpec with Matchers {

  import HappiestHourJob._

  val sparkConf: SparkConf = new SparkConf()
    .setMaster("local[*]")
    .setAppName(this.getClass.toString)

  implicit val spark: SparkSession = SparkSession.builder.config(sparkConf).getOrCreate()

  "happiestHourTimestamp" should "work if from < happiestHour" in {
    val from = DateTime.parse("2017-09-25T10:00Z").getMillis
    val happiestHour = 15
    val expected = DateTime.parse("2017-09-25T15:00Z").getMillis

    happiestHourTimestamp(from, happiestHour) shouldBe expected
  }

  "happiestHourTimestamp" should "work if from > happiestHour" in {
    val from = DateTime.parse("2017-09-25T16:00Z").getMillis
    val happiestHour = 15
    val expected = DateTime.parse("2017-09-26T15:00Z").getMillis

    happiestHourTimestamp(from, happiestHour) shouldBe expected
  }

  private object sampleHashtags {
    val David = "David"
    val Bowie = "Bowie"
    val Madonna = "Madonna"
    val Iggie = "Iggie"
    val Pop = "Pop"
  }

  val ts: Long = DateTime.parse("2017-09-25T00:00Z").getMillis
  val tweet = HappyTweet(
    id = 0L,
//    text = "test",
    hashtags = None,
    timestamp = ts)

  "findHappiestHour" should "work" in {
    val tweets = Seq(tweet,
      tweet.copy(timestamp = ts + 1*millisInHour),
      tweet.copy(timestamp = ts + 1*millisInHour),
      tweet.copy(timestamp = ts + 2*millisInHour),
      tweet.copy(timestamp = ts + 2*millisInHour),
      tweet.copy(timestamp = ts + 2*millisInHour)
    )

    val expected = 2

    val df = spark.createDataFrame(spark.sparkContext.parallelize(tweets))
      .withColumn("hour", hour(col("timestamp")))

    findHappiestHour(df) shouldBe expected
  }

  "findHashTags" should "work" in {
    import sampleHashtags._

    val tweets = Seq(tweet,
      tweet.copy(hashtags = Some(Seq(David, Bowie))),
      tweet.copy(hashtags = Some(Seq(David, Bowie, Madonna))),
      tweet.copy(hashtags = Some(Seq(Madonna)))
    )

    val expected = Seq(
      HashTags(Seq(David, Bowie)),
      HashTags(Seq(David, Bowie, Madonna))
    )

    val df = spark.createDataFrame(spark.sparkContext.parallelize(tweets))

    findHashTags(df, 2).collect() should contain theSameElementsAs expected
  }

  "hashTagsCombinations" should "work" in {
    import sampleHashtags._

    val hashtags = Seq(
      HashTags(Seq(David, Bowie)),
      HashTags(Seq(David, Bowie, Madonna))
    )

    val expected = Seq(
      HashTags(Seq(David, Bowie)),
      HashTags(Seq(David, Bowie)),
      HashTags(Seq(Madonna, David)),
      HashTags(Seq(Madonna, Bowie)),
      HashTags(Seq(Madonna, David, Bowie))
    )

    val df = spark.createDataFrame(spark.sparkContext.parallelize(hashtags))

    import spark.implicits._

    hashTagsCombinations(df.as[HashTags]).collect() should contain theSameElementsAs expected
  }
}
