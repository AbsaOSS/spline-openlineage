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

import org.apache.commons.configuration._
import za.co.absa.commons.config.ConfigurationImplicits.ConfigurationRequiredWrapper

import java.util.Properties
import scala.collection.JavaConverters._

class AggregatorConfig(config: Configuration) {

  def streamsConfig: Properties = ConfigurationConverter.getProperties(config.subset("spline.ol.streams"))

  def inputTopic = config.getRequiredString("spline.ol.inputTopic")
  def outputTopic = config.getRequiredString("spline.ol.outputTopic")


  def toMap: Map[String, AnyRef] =
    config.getKeys("spline")
      .asScala
      .asInstanceOf[Iterator[String]]
      .map(k => k -> config.getProperty(k))
      .toMap

}

object AggregatorConfig {
  private val DefaultPropertiesFileName = "spline.ol.default.properties"

  def apply: AggregatorConfig = {
    val configs = Seq(
      Some(new SystemConfiguration),
      Some(new PropertiesConfiguration(DefaultPropertiesFileName))
    )

    new AggregatorConfig(new CompositeConfiguration(configs.flatten.asJava))
  }
}
