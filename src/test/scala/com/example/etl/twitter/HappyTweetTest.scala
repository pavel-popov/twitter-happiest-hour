package com.example.etl.twitter

import org.joda.time.DateTime
import org.scalatest._

class HappyTweetTest extends FlatSpec with Matchers {

  "HappyTweet timestamp" should "be truncated to an hour" in {
    val tweet = HappyTweet(
      id = 0L,
//      text = "test",
      hashtags = None,
      timestamp = DateTime.parse("2017-09-25T20:59Z").getMillis
    )

    tweet.hour shouldBe DateTime.parse("2017-09-25T20:00Z").getMillis
  }

}
