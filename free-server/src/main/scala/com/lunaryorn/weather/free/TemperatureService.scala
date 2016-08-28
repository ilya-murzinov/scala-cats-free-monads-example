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

package com.lunaryorn.weather.free

import cats.data.{Xor, XorT}
import com.lunaryorn.weather.free.dsl._
import com.lunaryorn.weather.free.dsl.actions.Temperature._
import com.lunaryorn.weather.{TemperatureError, TemperatureValidator}
import squants.Temperature

class TemperatureService(validator: TemperatureValidator) {
  def addTemperature(temperature: Temperature)
    : TemperatureAction[Xor[TemperatureError, Temperature]] =
    XorT
      .fromXor[TemperatureAction](validator.validate(temperature).toXor)
      .leftMap(TemperatureError.InvalidTemperature)
      .flatMap(
        t =>
          XorT.right(store(t)): XorT[TemperatureAction,
                                     TemperatureError,
                                     Temperature])
      .value

  def getTemperatures: TemperatureAction[Seq[Temperature]] = getAll
}
