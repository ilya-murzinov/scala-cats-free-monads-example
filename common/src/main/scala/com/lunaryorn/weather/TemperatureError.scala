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

import io.circe.{Decoder, ObjectEncoder}
import io.circe.generic.semiauto._
import squants.{QuantityRange, Temperature}

sealed trait TemperatureError extends Exception

object TemperatureError {

  case class TemperatureOutOfBoundsError(range: QuantityRange[Temperature])
      extends TemperatureError

  object Codecs {
    import com.lunaryorn.weather.json._

    implicit val encodeTemperatureError: ObjectEncoder[TemperatureError] =
      deriveEncoder[TemperatureError]

    implicit val decodeTemperatureError: Decoder[TemperatureError] =
      deriveDecoder[TemperatureError]
  }
}
