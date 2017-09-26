package com.example.etl.twitter

import com.twitter.scalding.Args
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.sql.functions._
import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.LocalTime.MIDNIGHT
import com.typesafe.scalalogging.LazyLogging
import HappiestHourJobConfig._
import HappyTweetsJobConfig._
import org.apache.spark.sql.expressions.UserDefinedFunction

object HappiestHourJob extends LazyLogging {

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf()
      .setMaster(sparkMaster)
      .setAppName(this.getClass.toString)

    val params = Args(args)

    // period to analyse is [from, to) for provided timestamps (in millis)
    val from = DateTime.parse(params.required("from")).getMillis
    val to = DateTime.parse(params.required("to")).getMillis

    implicit val spark = SparkSession.builder.config(sparkConf).getOrCreate()
    try {

      val tweets = tweetsFromPath(from, to)
  //    tweets.show

      val happiestHour = findHappiestHour(tweets)
      logger.info(s"Happiest Hour is $happiestHour")

      val hashtags = findHashTags(tweets.where(col("hour") === lit(happiestHour)))
  //    hashtags.show

      val combinations = hashTagsCombinations(hashtags)
  //    combinations.show

      val mostPopularCombination = mostPopularHashTags(combinations)
      println(s"[${happiestHourTimestamp(from, happiestHour)}] ${mostPopularCombination.mkString(", ")}")
    }
    finally {
      spark.close()
    }
  }

  private def arrayLongerThan(len: Int) = udf((arr: Seq[String]) => arr.length >= len)
  private val distinct = udf((arr: Seq[String]) => arr.distinct)
  val hour: UserDefinedFunction = udf((ts: Long) => new DateTime(ts, DateTimeZone.UTC).getHourOfDay)
  val millisInHour: Long = 60*60*1000L

  /**
    * Returns most frequent hour for given tweets.
    * @param tweets   DataFrame with column "hour"
    * @return         Int from 0 to 23
    */
  def findHappiestHour(tweets: DataFrame): Int =
    tweets
      .groupBy(col("hour"))
      .count()
      .sort(col("count").desc)
      .collect()
      .head
      .getAs[Int]("hour")

  /**
    * Returns timestamp of given happiest hour.
    */
  def happiestHourTimestamp(from: Long, happiestHour: Int): Long = {
    val dt = new DateTime(from, DateTimeZone.UTC)
    val midnight = dt.toLocalDate.toDateTime(MIDNIGHT, DateTimeZone.UTC).getMillis

    midnight +
      happiestHour*millisInHour +
      (if (dt.getHourOfDay > happiestHour) 24*millisInHour else 0L)
  }

  /**
    * Finds tweets for given period [from, to) assuming timestamp presented in the path "hour=<timestamp>".
    */
  def tweetsFromPath(from: Long, to: Long)(implicit spark: SparkSession): DataFrame =
     spark
      .read.json(happyTweetsDir)
      .where(col("hour") >= lit(from) and col("hour") < lit(to))
      .withColumnRenamed("hour", "hourTs")
      .withColumn("hour", hour(col("hourTs")))  // get hour from file path assuming correct tweets placing
      .select(col("hashtags"), col("hour"))

  /**
    * Finds tweets for given period [from, to) getting timestamp from the tweets.
    */
  def tweetsFromTimestamp(from: Long, to: Long)(implicit spark: SparkSession): DataFrame =  {

    logger.info(s"from: $from, to: $to")

    val paths: Seq[String] =
      from until(to, millisInHour) map {
        hour => s"$happyTweetsDir/hour=$hour"
      }
    assert(paths.nonEmpty, "Empty Paths provided")
    logger.info(s"Paths: $paths")

    spark
      .read.json(paths: _*)
      .withColumn("hour", hour(col("timestamp")))  // get hour from file path assuming correct tweets placing
      .select(col("hashtags"), col("hour"))
  }

  /**
    * Returns tweets containing at least minHashTags of unique hashtags.
    */
  def findHashTags(tweets: DataFrame, minHashTags: Int = minHashTagsLength)(implicit spark: SparkSession): Dataset[HashTags] = {
    import spark.implicits._

    val hashtags = col("hashtags")

    val arrayLimit = arrayLongerThan(minHashTags)

    tweets
      .select(hashtags)
      .where(hashtags.isNotNull)
      .withColumn("hashtags", distinct(hashtags))
      .where(arrayLimit(hashtags))
      .as[HashTags]
  }

  /**
    * Returns combinations of hash tags.
    */
  def hashTagsCombinations(hashtags: Dataset[HashTags])(implicit spark: SparkSession): Dataset[HashTags] = {
    import spark.implicits._

    hashtags.flatMap { ht =>
      HashTags.combinations(ht.hashtags.toList).map {
        HashTags(_)
      }
    }
  }

  def mostPopularHashTags(hashtags: Dataset[HashTags]): Seq[String] = {
    val ht = col("hashtags")
    hashtags
      .groupBy(ht)
      .count()
      .sort(ht.desc)
      .head().getAs[Seq[String]](0)
  }
}

