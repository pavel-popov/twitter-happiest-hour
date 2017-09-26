package com.example.etl.twitter

import org.scalatest._

class HashTagsTest extends FlatSpec with Matchers {

  "HashTags combinations" should "work" in {

    val Bitterballen = "Bitterballen"
    val Cheese = "Cheese"
    val Ham = "Ham"

    val hashtags = List(Bitterballen, Cheese, Ham)

    HashTags.combinations(hashtags) shouldEqual
      List(List(Ham, Cheese, Bitterballen), List(Cheese, Bitterballen), List(Ham, Bitterballen), List(Ham, Cheese))
  }
}
