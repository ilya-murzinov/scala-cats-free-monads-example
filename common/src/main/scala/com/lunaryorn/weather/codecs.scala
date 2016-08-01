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

import com.twitter.util.Try
import io.circe.{JsonObject, ObjectEncoder}
import io.circe.syntax._
import io.finch.Decode
import squants.thermal.Temperature
import squants.{Temperature, UnitOfMeasure}

object codecs {
  implicit val decodeUnitOfMeasureTemperature: Decode[
      UnitOfMeasure[Temperature]] = Decode.instance { symbol =>
    Try.orThrow(Temperature.units.find(_.symbol == symbol))(() =>
          new IllegalArgumentException(s"Unknown unit symbol: $symbol"))
  }

  implicit val encodeException: ObjectEncoder[Exception] =
    ObjectEncoder.instance[Exception] { exception =>
      JsonObject.empty
        .add("type", exception.getClass.getName.asJson)
        .add("message", exception.getMessage.asJson)
    }
}
