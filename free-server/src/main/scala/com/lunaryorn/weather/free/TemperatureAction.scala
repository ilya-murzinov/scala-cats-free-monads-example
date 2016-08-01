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

import cats.free.Free
import cats.free.Free.liftF
import squants.Temperature

sealed trait TemperatureActionT[T]

object TemperatureActionT {
  case object GetAll extends TemperatureActionT[Seq[Temperature]]
  case class Store(temperature: Temperature)
      extends TemperatureActionT[Temperature]
}

object TemperatureAction {
  type TemperatureAction[T] = Free[TemperatureActionT, T]

  val getAll: TemperatureAction[Seq[Temperature]] = liftF(
      TemperatureActionT.GetAll)

  def store(temperature: Temperature): TemperatureAction[Temperature] =
    liftF(TemperatureActionT.Store(temperature))

  def pure[T](t: T): TemperatureAction[T] = Free.pure(t)
}
