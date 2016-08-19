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

import cats.{Id, ~>}
import com.lunaryorn.weather.{TemperatureRepository, TemperatureValidator}
import com.twitter.util.Future

object Interpreters {
  import TemperatureStoreActionT._
  import TemperatureActionT._

  def interpretTemperatureStoreActionWithRepository(
      repo: TemperatureRepository
  ): TemperatureStoreActionT ~> Future =
    new (TemperatureStoreActionT ~> Future) {
      override def apply[A](action: TemperatureStoreActionT[A]): Future[A] =
        action match {
          case GetAll => repo.getTemperatures
          case Store(temperature) => repo.addTemperature(temperature)
        }
    }

  def interpretTemperatureActionWithValidator(
      validator: TemperatureValidator): TemperatureActionT ~> Id =
    new (TemperatureActionT ~> Id) {
      override def apply[A](
        action: TemperatureActionT[A]
      ): Id[A] = action match {
        case Validate(temperature) => validator.validate(temperature)
      }
    }
}
