package com.example.etl.twitter

import com.danielasfregola.twitter4s.entities.Tweet

/**
  * HappyTweet represents tweet structure required for HappiestHourJob.
  * @param id: tweet id
  * @param text: tweet text
  * @param hashtags: Seq of HashTag presented in the tweet
  * @param timestamp: timestamp of the tweet (in milliseconds)
  */
case class HappyTweet (
  id:        Long,
  text:      String,
  hashtags:  Seq[String],
  timestamp: Long
) {
  def hour: Long = {
    timestamp / 1000 / 3600 * 3600 * 1000
  }
}

object HappyTweet {
  def apply(tweet: Tweet): HappyTweet = {
    HappyTweet(
      id = tweet.id,
      text = tweet.text,
      hashtags = tweet.entities.map(_.hashtags.map(_.text)).getOrElse(Seq()),
      timestamp = tweet.created_at.getTime
    )
  }
}
