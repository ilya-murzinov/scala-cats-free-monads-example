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
import com.lunaryorn.weather.free.TemperatureAction.TemperatureAction
import com.lunaryorn.weather.{InMemoryWeatherRepository, TemperatureError}
import com.twitter.finagle.Http
import com.twitter.finagle.http.Status
import com.twitter.util.Await
import io.catbird.util._
import io.finch._
import io.finch.circe._
import squants.thermal.TemperatureConversions._
import squants.{Temperature, UnitOfMeasure}

object FreeServer extends App {
  import com.lunaryorn.weather.codecs.encodeException
  import com.lunaryorn.weather.json._
  import TemperatureError.Codecs._

  val weatherService = new WeatherService(
      -100.degreesCelsius to 150.degreesCelsius)
  val repository = new InMemoryWeatherRepository

  type TemperatureEndpoint[T] = Endpoint[TemperatureAction[Output[T]]]

  val postTemperature: TemperatureEndpoint[Temperature] =
    post("temperatures" :: body.as[Temperature]).map {
      temperature: Temperature =>
        XorT(weatherService.addTemperature(temperature))
          .leftMap(TemperatureError.toRequestError)
          .fold(BadRequest, Created)
    }

  val getTemperatures: TemperatureEndpoint[Seq[Temperature]] = {
    import com.lunaryorn.weather.codecs.decodeUnitOfMeasureTemperature
    get("temperatures" :: paramOption("unit").as[UnitOfMeasure[Temperature]]).map {
      unit: Option[UnitOfMeasure[Temperature]] =>
        for {
          temperatures <- weatherService.getTemperatures
        } yield
          Ok(
              unit
                .map(unit => temperatures.map(t => unit(t.to(unit))))
                .getOrElse(temperatures))
    }
  }

  val interpreter = Interpreters.interpretWithRepository(repository)
  def interpret[T](action: TemperatureAction[T]) = action.foldMap(interpreter)
  val endpoints = getTemperatures.mapOutputAsync(interpret) :+: postTemperature
      .mapOutputAsync(interpret)

  Await.ready(Http.server.serve("127.0.0.1:8080", endpoints.toService))
}
