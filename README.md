# Twitter Happiest Hour

Example ETL process which determines the happiest hour on Twitter. The 
happiest hour of the day is the one containing the most of ":)" in tweets.

Note: All time-specific calculations are performed in UTC TimeZone.


## Prerequisites

Download Spark 2.2.0 from https://spark.apache.org/downloads.html (Pre-build for
Hadoop 2.6) - spark-2.2.0-bin-hadoop2.6.tgz, untar, create env variable
`SPARK_HOME` pointing to the location.

## Configuration

Configuration to be placed in the `src/main/resources/application.conf` file and should contain valid Twitter API Keys.

## Test and Compile

    sbt clean assembly

## First part

Reads Twitter Streaming API, filters incoming tweets containing ":)", saves to json files into
`HappyTweetsJob.happyTweetsDir` into folders partitioned by hour: `hour=<hour-timestamp>`.

Batch size is determined by setting `HappyTweetsJob.batchSize`.

Each json file is named as `tweets-<first-tweet-id>.json`.

Run:

    java \
        -cp target/scala-2.11/twitter-happiest-hour-assembly-1.0.jar \
        -Dconfig.file=src/main/resources/application.conf \
        com.example.etl.twitter.HappyTweetsJob

## Second part

One can find happiest hour during arbitrary time period `[from, to)`.

Run:

    sbt \
        -Dconfig.file=src/main/resources/application.conf \
        "runMain com.example.etl.twitter.HappiestHourJob \
        --from 2017-09-25T14:00Z \
        --to 2017-09-25T16:00Z"

    java \
        -classpath "target/scala-2.11/twitter-happiest-hour-assembly-1.0.jar:$SPARK_HOME/jars/*" \
        -Dconfig.file=src/main/resources/application.conf \
        com.example.etl.twitter.HappiestHourJob \
        --from 2017-09-25T14:00Z \
        --to 2017-09-25T16:00Z

## TODO

- Fix transitive dependencies conflicting versions (json4s, jackson, netty, etc.)
