package com.example.etl.twitter

import HappiestHourJobConfig.minHashTagsLength

case class HashTags(
  hashtags: Seq[String]
)

object HashTags {
  def combinations(seq: List[String]): List[List[String]] = {

    def comb(active: List[String], rest: List[String]): List[List[String]] = {
      if (rest.isEmpty) {
        List(active)
      } else {
        comb(rest.head :: active, rest.tail) ++ comb(active, rest.tail)
      }
    }

    comb(Nil, seq.sorted).filter(_.length >= minHashTagsLength)
  }
}