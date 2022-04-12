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

package za.co.absa.spline.ol.rest

import com.typesafe.scalalogging.StrictLogging
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.json4s.ext.{JavaTimeSerializers, JavaTypesSerializers}
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.{BadRequest, Ok, ScalatraServlet}
import za.co.absa.spline.ol.json.InstantSerializer
import za.co.absa.spline.ol.model.openlineage.v0_3_1.RunEvent

import scala.util.{Failure, Success, Try}

class LineageServlet extends ScalatraServlet with JacksonJsonSupport with StrictLogging {

  protected implicit lazy val jsonFormats: Formats =
    DefaultFormats ++ JavaTypesSerializers.all ++ JavaTimeSerializers.all + InstantSerializer

  logger.info(s"Config:\n${RestConfig.toMap.map{ case (k, v) => s"\t$k = $v"}.mkString("\n")}")

  val kafkaProducer = new KafkaProducer[String, String](RestConfig.producerConfig)
  sys.addShutdownHook(kafkaProducer.close())


  post("/") {

    Try(parsedBody.extract[RunEvent]) match {
      case Failure(e) =>
        logger.debug("Exception when parsing request body", e)
        BadRequest()
      case Success(runEvent) =>
        val key = runEvent.run.runId.toString
        val value = request.body
        val record = new ProducerRecord[String, String](RestConfig.topic, key, value)
        kafkaProducer.send(record).get()
        Ok
    }
  }
}
