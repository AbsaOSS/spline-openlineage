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

import org.json4s.JsonAST.JValue
import za.co.absa.spline.harvester.IdGenerator.{UUIDGeneratorFactory, UUIDNamespace}
import za.co.absa.spline.ol.aggregator.JsonSerDe._
import za.co.absa.spline.ol.model.openlineage.v0_3_1.{InputDataset, OutputDataset, RunEvent}
import za.co.absa.spline.producer.model.v1_2._

import java.util.UUID

object OpenLineageToSplineConverter {

  private val execPlanIdGenerator = UUIDGeneratorFactory.forVersion(5)(UUIDNamespace.ExecutionPlan)

  def convert(key: String, value: JValue): Iterable[(String, String)] = {
    implicit val formats = JsonSerDe._formats
    val runEvent = value.extract[RunEvent]

    val outputIsEmpty = runEvent.outputs.map(_.isEmpty).getOrElse(true)
    if (outputIsEmpty) {
      Seq.empty
    } else {
      val planEventSeq = convert(runEvent)
      planEventSeq.map(key -> _.toJson)
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
        extraInfo = Map.empty
      )

      val planId = execPlanIdGenerator.nextId(planWithoutId)
      val plan = planWithoutId.copy(id = planId)

      val event = ExecutionEvent(
        planId = plan.id,
        labels = Map.empty,
        timestamp = runEvent.eventTime.toEpochMilli,
        durationNs = None,
        discriminator = None,
        error = None,
        extra = Map.empty
      )

      (plan, event)
    }
  }

  private def convertInputs(inputs: Seq[InputDataset]): Seq[ReadOperation] = {
    inputs.map(in => ReadOperation(
      inputSources = Seq(toUri(in.name, in.namespace)),
      id = UUID.randomUUID().toString,
      name = None,
      output = None,
      params = Map.empty,
      extra = Map.empty
    ))
  }

  private def convertOutput(output: OutputDataset, inputIds: Seq[String]): WriteOperation = {
    WriteOperation(
      outputSource = toUri(output.name, output.namespace),
      append = false, // TODO ???
      id = UUID.randomUUID().toString,
      name = None,
      childIds = inputIds,
      params = Map.empty,
      extra = Map.empty
    )
  }

  private def toUri(name: String, namespace: String): String = {
    if (name.startsWith("/"))
      s"$namespace$name"
    else
      s"$namespace/$name"
  }

}
