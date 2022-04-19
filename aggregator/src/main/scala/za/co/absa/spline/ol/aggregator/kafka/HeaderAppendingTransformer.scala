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

package za.co.absa.spline.ol.aggregator.kafka

import org.apache.kafka.streams.kstream.ValueTransformer
import org.apache.kafka.streams.processor.ProcessorContext
import za.co.absa.spline.ol.aggregator.ValueAndHeaders

class HeaderAppendingTransformer extends ValueTransformer[ValueAndHeaders, String] {

  private var context: ProcessorContext = null

  override def init(processorContext: ProcessorContext): Unit = {
    context = processorContext
  }

  override def transform(valueAndHeaders: ValueAndHeaders): String = {
    valueAndHeaders.headers.foreach(context.headers.add(_))
    valueAndHeaders.value
  }

  override def close(): Unit = {}
}
