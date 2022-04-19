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

package za.co.absa.spline.ol.aggregator.json

import org.json4s.Extraction.decompose
import org.json4s.JsonAST.JValue
import org.json4s.ext.JavaTypesSerializers
import org.json4s.{DefaultFormats, Formats, jackson}
import za.co.absa.spline.ol.json.InstantSerializer

object JsonSerDe extends jackson.JsonMethods {

  implicit val _formats: Formats = DefaultFormats ++ JavaTypesSerializers.all + InstantSerializer

  def toJson(entity: AnyRef): String = compact(render(decompose(entity)))

  def fromJValue[T: Manifest](jv: JValue): T = {
    jv.extract[T]
  }
}
