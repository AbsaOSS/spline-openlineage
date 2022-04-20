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

import za.co.absa.spline.ol.model.openlineage.v0_3_1.RunEvent
import za.co.absa.spline.producer.model.v1_2.NameAndVersion

import java.net.URI
import scala.util.{Failure, Success, Try}

object SystemInfoExtractor {

  val UnknownNameAndVersion: NameAndVersion = NameAndVersion("UNKNOWN", "0.0.0")

  def extract(runEvent: RunEvent): NameAndVersion = {
    Try(URI.create(runEvent.producer)) match {
      case Success(producerUri) => extractFromProducerURI(producerUri)
      case Failure(_) => UnknownNameAndVersion
    }
  }

  private val openLineageREgex = "/tree/(\\d+.\\d+.\\d+)/integration/([^/]+)".r

  def extractFromProducerURI(producerUri: URI): NameAndVersion = {
    if (producerUri.getHost == "github.com" && producerUri.getPath.toLowerCase.startsWith("/openlineage")) {
      openLineageREgex
        .findFirstMatchIn(producerUri.getPath)
        .map(m => NameAndVersion(m.group(2), m.group(1)))
        .getOrElse(UnknownNameAndVersion)
    } else {
      UnknownNameAndVersion
    }
  }

}
