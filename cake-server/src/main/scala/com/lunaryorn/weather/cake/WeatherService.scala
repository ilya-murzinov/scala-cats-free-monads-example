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
import com.lunaryorn.weather.WeatherRepositoryComponent
import com.twitter.util.Future
import io.catbird.util._
import io.circe.generic.semiauto._
import io.circe.{Decoder, ObjectEncoder}
import squants.thermal.TemperatureConversions._
import squants.{QuantityRange, Temperature}

sealed trait TemperatureError
case class TemperatureOutOfBoundsError(range: QuantityRange[Temperature])
    extends TemperatureError

object TemperatureError {
  object Codecs {
    import com.lunaryorn.weather.json._

    implicit val encodeTemperatureError: ObjectEncoder[TemperatureError] =
      deriveEncoder[TemperatureError]

    implicit val decodeTemperatureError: Decoder[TemperatureError] =
      deriveDecoder[TemperatureError]
  }
}

trait WeatherService {
  def addTemperature(
      temperature: Temperature): Future[Xor[TemperatureError, Temperature]]

  def getTemperatures: Future[Seq[Temperature]]
}

trait WeatherServiceComponent {
  def weatherService: WeatherService
}

trait WeatherServiceComponentImpl { self: WeatherRepositoryComponent =>

  val weatherService: WeatherService = new WeatherServiceImpl

  val temperatureRange = -100.degreesCelsius.to(150.degreesCelsius)

  private class WeatherServiceImpl extends WeatherService {
    override def addTemperature(
        temperature: Temperature
    ): Future[Xor[TemperatureError, Temperature]] =
      (if (temperatureRange.contains(temperature)) {
         XorT.right(weatherRepository.addTemperature(temperature))
       } else {
         XorT.fromXor[Future](
             Xor.left(TemperatureOutOfBoundsError(temperatureRange)))
       }).value

    override def getTemperatures: Future[Seq[Temperature]] =
      weatherRepository.getTemperatures
  }
}
