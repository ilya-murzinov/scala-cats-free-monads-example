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

import com.twitter.util.Future
import squants.Temperature

import scala.collection.mutable

trait TemperatureRepository {

  def addTemperature(temperature: Temperature): Future[Temperature]

  def getTemperatures: Future[Seq[Temperature]]
}

class InMemoryTemperatureRepository extends TemperatureRepository {
  private lazy val temperatures: mutable.ArrayBuffer[Temperature] =
    mutable.ArrayBuffer()

  override def addTemperature(temperature: Temperature): Future[Temperature] = {
    temperatures += temperature
    Future.value(temperature)
  }

  override def getTemperatures: Future[Seq[Temperature]] =
    Future.value(temperatures)
}
