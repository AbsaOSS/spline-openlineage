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

import org.apache.commons.configuration.Configuration

import scala.collection.JavaConverters.asScalaIteratorConverter

object AggConfigurationImplicits {

//  implicit class ConfigurationRequiredWrapper[T <: Configuration](val conf: T) extends AnyVal {
//
//    def toMap: Map[String, AnyRef] = toMap(conf.getKeys())
//
//    def toMap(prefixFilter: String): Map[String, AnyRef] = toMap(conf.getKeys(prefixFilter))
//
//    private def toMap(keys: java.util.Iterator[_]): Map[String, AnyRef] =
//      keys
//        .asScala
//        .asInstanceOf[Iterator[String]]
//        .map(k => k -> conf.getProperty(k))
//        .toMap
//  }
}
