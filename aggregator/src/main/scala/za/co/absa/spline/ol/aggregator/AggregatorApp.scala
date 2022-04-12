/*
 * Copyright 2022 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.spline.ol.aggregator

import com.typesafe.scalalogging.StrictLogging
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.kstream.SessionWindows
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala.Serdes._
import org.apache.kafka.streams.scala.kstream.Materialized
import org.apache.kafka.streams.scala.{Serdes, StreamsBuilder}
import org.json4s.JsonAST.JValue
import org.json4s.{DefaultFormats, JNothing}

import java.time.Duration

object AggregatorApp extends StrictLogging{

  implicit def serde: Serde[JValue] = {
    val serializer = (a: JValue) => JsonSerDe.compact(JsonSerDe.render(a)).getBytes
    val deserializer = (aAsBytes: Array[Byte]) => {
      val aAsString = new String(aAsBytes)
      val jValue = JsonSerDe.parse(aAsString)
      Option(jValue)
    }

    Serdes.fromFn[JValue](serializer, deserializer)
  }

  def main(args: Array[String]): Unit = {
    logger.info(s"Starting Spline Open Lineage Aggregator ${AggregatorBuildInfo.Version}")
    val config = AggregatorConfig.apply

    logger.info(s"Config:\n${config.toMap.map{ case (k, v) => s"\t$k = $v"}.mkString("\n")}")

    val window = SessionWindows.`with`(Duration.ofMinutes(1))

    val builder = new StreamsBuilder
    val inputStream = builder.stream[String, JValue](config.inputTopic)
    val table = inputStream
      .groupByKey
      .windowedBy(window)
      .aggregate(JNothing: JValue)(
        (key, msg, aggMsg) => aggMsg.merge(msg),
        (key, msg, aggMsg) => aggMsg.merge(msg)
      )(Materialized.as("my-store"))

    //topic name: spline-open-lineage-aggregator-my-store-changelog

    table.toStream
      .peek((k,v) => println("\n" + k + " -> " + v + " : " + extractEventType(v)))
      .filter((k, v) => extractEventType(v) == "COMPLETE")
      .map((windowedKey, value) => (windowedKey.key(), value))
      .peek((k,v) => println("\n" + k + " -> " + v + " : " + extractEventType(v)))
      .flatMap(OpenLineageToSplineConverter.convert)
      .peek((k,v) => println("\n" + k + " -> " + v))
      .to(config.outputTopic)

    val topology = builder.build
    val streams = new KafkaStreams(topology, config.streamsConfig)

    streams.start()
    logger.info("Streams started")


    sys.addShutdownHook(streams.close())
  }

  private def extractEventType(json: JValue): String = {
    implicit val formats = DefaultFormats

    (json \ "eventType").extract[String]
  }


}
