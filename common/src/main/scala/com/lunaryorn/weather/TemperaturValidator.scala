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

import cats.data.Validated.{Invalid, Valid}
import cats.data.{Validated, ValidatedNel}
import io.circe.ObjectEncoder
import io.circe.generic.semiauto._
import squants.{Quantity, QuantityRange, Temperature}

sealed trait TemperatureValidationError

object TemperatureValidationError {
  case class TemperatureOutOfBoundsError(range: QuantityRange[Temperature])
      extends TemperatureValidationError

  object Codecs {
    import com.lunaryorn.weather.json._

    implicit val encodeTemperatureValidationError: ObjectEncoder[
        TemperatureValidationError] = deriveEncoder[TemperatureValidationError]
  }
}

trait TemperatureValidator {
  def validate(temperature: Temperature)
    : Validated[TemperatureValidationError, Temperature]
}

class TemperatureRangeValidator(val range: QuantityRange[Temperature])
    extends TemperatureValidator {
  import TemperatureValidationError._

  override def validate(temperature: Temperature)
    : Validated[TemperatureValidationError, Temperature] = {
    if (range.includes(temperature)) Valid(temperature)
    else Invalid(TemperatureOutOfBoundsError(range))
  }
}
