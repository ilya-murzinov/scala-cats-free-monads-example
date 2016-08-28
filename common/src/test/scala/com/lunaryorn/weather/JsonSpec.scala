/*
 * Copyright 2016 Sebastian Wiesner
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

package com.lunaryorn.weather

import cats.data.Xor
import com.lunaryorn.weather.prop._
import io.circe.Json
import io.circe.syntax._
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import squants.UnitOfMeasure
import squants.thermal.Temperature

class JsonSpec
    extends WordSpec
    with MustMatchers
    with OptionValues
    with GeneratorDrivenPropertyChecks {

  import json._

  "UnitOfMeasure[Temperature]" must {
    "deserialize from JSON" in {
      forAll { unit: UnitOfMeasure[Temperature] =>
        val json = Json.fromString(unit.symbol)
        json.as[UnitOfMeasure[Temperature]] mustBe Xor.right(unit)
      }
    }

    "serialize to JSON" in {
      forAll { unit: UnitOfMeasure[Temperature] =>
        unit.asJson mustBe Json.fromString(unit.symbol)
      }
    }

    "roundtrip to and from JSON" in {
      forAll { unit: UnitOfMeasure[Temperature] =>
        unit.asJson.as[UnitOfMeasure[Temperature]] mustBe Xor.right(unit)
      }
    }
  }

  "Temperature" must {
    "deserialize from JSON" in {
      forAll { temperature: Temperature =>
        val json = Json.obj(
          "value" -> temperature.value.asJson,
          "unit" -> (temperature.unit: UnitOfMeasure[Temperature]).asJson
        )
        json.as[Temperature] mustBe Xor.right(temperature)
      }
    }

    "serialize to JSON" in {
      forAll { temperature: Temperature =>
        temperature.asJson mustBe Json.obj(
          "value" -> temperature.value.asJson,
          "unit" -> (temperature.unit: UnitOfMeasure[Temperature]).asJson
        )
      }
    }

    "roundtrip to and from JSON" in {
      forAll { temperature: Temperature =>
        temperature.asJson.as[Temperature] mustBe Xor.right(temperature)
      }
    }
  }
}
