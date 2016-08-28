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

package com.lunaryorn.weather.cake

import cats.data.{Xor, XorT}
import com.lunaryorn.weather.{
  TemperatureError,
  TemperatureRepositoryComponent,
  TemperatureValidatorComponent
}
import com.twitter.util.Future
import io.catbird.util._
import squants.Temperature
import squants.thermal.TemperatureConversions._

trait TemperatureService {
  def addTemperature(
      temperature: Temperature): Future[Xor[TemperatureError, Temperature]]

  def getTemperatures: Future[Seq[Temperature]]
}

trait TemperatureServiceComponent {
  def temperatureService: TemperatureService
}

trait TemperatureServiceComponentImpl extends TemperatureServiceComponent {
  self: TemperatureRepositoryComponent with TemperatureValidatorComponent =>

  override val temperatureService: TemperatureService = new WeatherServiceImpl

  private class WeatherServiceImpl extends TemperatureService {
    override def addTemperature(
        temperature: Temperature): Future[Xor[TemperatureError, Temperature]] =
      XorT
        .fromXor[Future](temperatureValidator.validate(temperature).toXor)
        .leftMap(TemperatureError.InvalidTemperature)
        .flatMap(t =>
          XorT.right(temperatureRepository.addTemperature(temperature)): XorT[
            Future,
            TemperatureError,
            Temperature])
        .value

    override def getTemperatures: Future[Seq[Temperature]] =
      temperatureRepository.getTemperatures
  }
}
