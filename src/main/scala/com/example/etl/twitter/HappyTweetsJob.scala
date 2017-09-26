package com.example.etl.twitter

import java.nio.file.{Files, Paths}
import java.nio.charset.StandardCharsets

import scala.collection.mutable.Buffer
import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.streaming.StreamingMessage
import com.danielasfregola.twitter4s.entities.Tweet
import org.json4s.jackson.Serialization.write
import org.json4s.DefaultFormats
import com.typesafe.scalalogging.LazyLogging
import HappyTweetsJobConfig._


/**
  * HappyTweetsJob saves Happy Tweets (with ':)' in the message) to a set of files partitioned by UTC hour.
  */
object HappyTweetsJob extends LazyLogging {

  private val tweets: Buffer[Tweet] = Buffer.empty

  implicit val formats = DefaultFormats

  def main(args: Array[String]): Unit = {
    logger.info("App is starting")

    val client = TwitterStreamingClient()

    client.sampleStatuses()(happyTweets)
//    client.firehoseStatuses(count=Some(100))(happyTweets)
  }

  /**
    * Dumps provided tweets to file as json.
    */
  private def dump(tweets: Seq[HappyTweet]) = {
    val tweetsByHour = tweets.groupBy(_.hour)
    tweetsByHour.foreach {case (hour, tweets) =>
      val filename = Paths.get(s"$happyTweetsDir/hour=$hour/tweets-${tweets.head.id}.json")
      Files.createDirectories(filename.getParent)
      Files.write(filename, write(tweets).getBytes(StandardCharsets.UTF_8))
    }
  }

  /**
    * Saves provided tweet to current buffer. If buffer overgrown its capacity calls dump on disk.
    */
  private def save(tweet: Tweet) = {
    if (tweets.length > batchSize) {
      logger.info("Saving tweets")
      val happyTweets: Seq[HappyTweet] = tweets.map(HappyTweet(_))
      dump(happyTweets)

      tweets.clear()
    } else {
      logger.info(s"Tweet found: ${tweet.text}")
      tweets += tweet
    }
  }

  private def happyTweets: PartialFunction[StreamingMessage, Unit] = {
    case tweet: Tweet => if (tweet.text.contains(":)".toCharArray)) save(tweet)
  }
}
