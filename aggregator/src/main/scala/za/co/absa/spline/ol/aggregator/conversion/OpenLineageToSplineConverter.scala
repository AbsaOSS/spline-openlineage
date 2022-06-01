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

import com.fasterxml.uuid.Generators
import org.json4s.JsonAST.JValue
import za.co.absa.spline.ol.aggregator.json.JsonSerDe
import za.co.absa.spline.ol.aggregator.json.JsonSerDe.fromJValue
import za.co.absa.spline.ol.aggregator.{AggregatorBuildInfo, SystemInfoExtractor, ValueAndHeaders}
import za.co.absa.spline.ol.model.openlineage.v0_3_1.{InputDataset, OutputDataset, RunEvent}
import za.co.absa.spline.producer.model.v1_2._

import java.security.MessageDigest
import java.util.UUID

object OpenLineageToSplineConverter {

  val ProducerApiVersion = "v1.2"

  val planHeaders = Map("__TypeId__" -> "ExecutionPlan", "ABSA-Spline-API-Version" -> ProducerApiVersion)
  val eventHeaders = Map("__TypeId__" -> "ExecutionEvent", "ABSA-Spline-API-Version" -> ProducerApiVersion)


  def convert(key: String, value: JValue): Iterable[(String, ValueAndHeaders)] = {
    val runEvent = fromJValue[RunEvent](value)

    val outputIsEmpty = runEvent.outputs.map(_.isEmpty).getOrElse(true)
    if (outputIsEmpty) {
      Seq.empty
    } else {
      val planEventSeq = convert(runEvent)
      planEventSeq.flatMap { case (p, e) =>
        Seq(
          p.id.toString -> new ValueAndHeaders(JsonSerDe.toJson(p), planHeaders),
          e.planId.toString -> new ValueAndHeaders(JsonSerDe.toJson(e), eventHeaders)
        )
      }
    }
  }

  def convert(runEvent: RunEvent): Seq[(ExecutionPlan, ExecutionEvent)] = {

    val olInputs = runEvent.inputs.getOrElse(Seq.empty)
    val inputs = convertInputs(olInputs)
    val olOutputs = runEvent.outputs.getOrElse(Seq.empty)
    val outputs = olOutputs.map(convertOutput(_, inputs.map(_.id)))

    outputs.map { output =>
      val operations = Operations(
        write = output,
        reads = inputs,
        other = Seq.empty
      )

      val planWithoutId = ExecutionPlan(
        id = null,
        name = Some(runEvent.job.name),
        discriminator = None,
        labels = Map.empty,
        operations = operations,
        attributes = Seq.empty,
        expressions = None,
        systemInfo = SystemInfoExtractor.extract(runEvent),
        agentInfo = Some(NameAndVersion(s"spline-open-lineage-aggregator", AggregatorBuildInfo.Version)),
        extraInfo = createExtra(runEvent.job.facets)
      )

      val planId = generateId(planWithoutId)
      val plan = planWithoutId.copy(id = planId)

      val event = ExecutionEvent(
        planId = plan.id,
        labels = Map.empty,
        timestamp = runEvent.eventTime.toEpochMilli,
        durationNs = None,
        discriminator = None,
        error = None,
        extra = createExtra(runEvent.run.facets)
      )

      (plan, event)
    }
  }

  private def convertInputs(inputs: Seq[InputDataset]): Seq[ReadOperation] = {
    inputs.map(in => ReadOperation(
      inputSources = Seq(UriConverter.convert(in.namespace, in.name)),
      id = UUID.randomUUID().toString,
      name = None,
      output = None,
      params = Map.empty,
      extra = createExtra(in.facets) ++
        in.inputFacets.map(fs => Map("inputFacets" -> fs)).getOrElse(Map.empty)
    ))
  }

  private def convertOutput(output: OutputDataset, inputIds: Seq[String]): WriteOperation = {
    WriteOperation(
      outputSource = UriConverter.convert(output.namespace, output.name),
      append = false,
      id = UUID.randomUUID().toString,
      name = None,
      childIds = inputIds,
      params = Map.empty,
      extra = createExtra(output.facets) ++
        output.outputFacets.map(fs => Map("outputFacets" -> fs)).getOrElse(Map.empty)
    )
  }

  private def createExtra(facets: Option[Map[String, Any]]): Map[String, Any] =
    facets
      .map(facetMap => Map("facets" -> facetMap))
      .getOrElse(Map.empty)

  private val ExecutionPlanUUIDNamespace: UUID = UUID.fromString("475196d0-16ca-4cba-aec7-c9f2ddd9326c")

  private def generateId(entity: AnyRef): UUID = {
    val input = JsonSerDe.toJson(entity)
    val digest = MessageDigest.getInstance("SHA-1")
    val generator = Generators.nameBasedGenerator(ExecutionPlanUUIDNamespace, digest)
    generator.generate(input)
  }

}
