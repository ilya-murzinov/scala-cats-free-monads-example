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
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._
import squants.thermal._
import squants.{QuantityRange, UnitOfMeasure}

object json {

  implicit val encodeTemperatureUnitOfMeasure: Encoder[
      UnitOfMeasure[Temperature]] =
    Encoder.instance[UnitOfMeasure[Temperature]](unit =>
          Json.fromString(unit.symbol))

  implicit val decodeTemperatureUnitOfMeasure: Decoder[
      UnitOfMeasure[Temperature]] =
    Decoder.instance[UnitOfMeasure[Temperature]] { cursor =>
      for {
        symbol <- cursor.as[String]
        unit <- Xor.fromOption(
                   Temperature.units.find(_.symbol == symbol),
                   DecodingFailure("TemperatureScale", cursor.history))
      } yield unit
    }

  implicit val encodeTemperature: ObjectEncoder[Temperature] =
    ObjectEncoder.instance[Temperature] { temperature =>
      JsonObject.empty
        .add("value", temperature.value.asJson)
        .add("unit", encodeTemperatureUnitOfMeasure(temperature.unit))
    }

  implicit val decodeTemperature: Decoder[Temperature] =
    Decoder.instance[Temperature] { cursor =>
      for {
        value <- cursor.downField("value").as[Double]
        unit <- cursor.downField("unit").as[UnitOfMeasure[Temperature]]
      } yield unit(value)
    }

  implicit val encodeTemperatureQuantityRange: ObjectEncoder[
      QuantityRange[Temperature]] = deriveEncoder[QuantityRange[Temperature]]

  implicit val decodeTemperatureQuantityRange: Decoder[
      QuantityRange[Temperature]] = deriveDecoder[QuantityRange[Temperature]]
}
