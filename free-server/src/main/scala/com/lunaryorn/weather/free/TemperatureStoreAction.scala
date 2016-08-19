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

sealed trait TemperatureStoreActionT[T]

object TemperatureStoreActionT {
  case object GetAll extends TemperatureStoreActionT[Seq[Temperature]]
  case class Store(temperature: Temperature)
      extends TemperatureStoreActionT[Temperature]
}

object TemperatureStoreAction {
  type TemperatureStoreAction[T] = Free[TemperatureStoreActionT, T]

  val getAll: TemperatureStoreAction[Seq[Temperature]] = liftF(
      TemperatureStoreActionT.GetAll)

  def store(temperature: Temperature): TemperatureStoreAction[Temperature] =
    liftF(TemperatureStoreActionT.Store(temperature))

  def const[T](t: T): TemperatureStoreAction[T] = Free.pure(t)
}
