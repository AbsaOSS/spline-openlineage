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

package za.co.absa.spline.ol.aggregator.conversion

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import za.co.absa.spline.ol.aggregator.SystemInfoExtractor
import za.co.absa.spline.ol.model.openlineage.v0_3_1.RunEvent

class UriConverterSpec extends AnyFlatSpec with Matchers {

  it should "convert postgresql to spline format" in {
    UriConverter.convert(
      "postgres://host.docker.internal:5433",
      "postgres.public.foo"
    ) shouldEqual("jdbc:postgresql://localhost:5433/postgres:public.foo")
  }

}
