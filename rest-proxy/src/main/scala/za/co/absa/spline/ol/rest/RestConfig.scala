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

import org.apache.commons.configuration.{CompositeConfiguration, Configuration, ConfigurationConverter, PropertiesConfiguration, SystemConfiguration}
import za.co.absa.commons.config.ConfigurationImplicits.ConfigurationRequiredWrapper

import java.util.Properties
import scala.collection.JavaConverters._

object RestConfig {
  private val DefaultPropertiesFileName = "spline.ol.default.properties"

  private val config = new CompositeConfiguration(Seq(
    Some(new SystemConfiguration),
    Some(new PropertiesConfiguration(DefaultPropertiesFileName))
  ).flatten.asJava)

  def producerConfig: Properties = ConfigurationConverter.getProperties(config.subset("spline.ol.producer"))

  def topic = config.getRequiredString("spline.ol.topic")

  def toMap: Map[String, AnyRef] =
    config.getKeys("spline")
      .asScala
      .asInstanceOf[Iterator[String]]
      .map(k => k -> config.getProperty(k))
      .toMap
}
