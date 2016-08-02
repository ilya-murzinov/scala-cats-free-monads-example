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

import cats.data.Xor
import com.lunaryorn.weather.json._
import com.lunaryorn.weather.{InMemoryWeatherRepositoryComponentImpl, TemperatureError}
import com.lunaryorn.weather.TemperatureError.Codecs._
import com.twitter.finagle.Http
import com.twitter.finagle.http.Status
import com.twitter.util.Await
import io.finch._
import io.finch.circe._
import squants.UnitOfMeasure
import squants.thermal._

object CakeServer
    extends App
    with WeatherServiceComponentImpl
    with InMemoryWeatherRepositoryComponentImpl {

  import com.lunaryorn.weather.codecs.encodeException

  val postTemperature: Endpoint[Xor[TemperatureError, Temperature]] =
    post("temperatures" :: body.as[Temperature]) { temperature: Temperature =>
      weatherService.addTemperature(temperature).map {
        case r @ Xor.Right(_) => Ok(r)
        case l @ Xor.Left(error) => Output.payload(l, Status.BadRequest)
      }
    }

  val getTemperatures: Endpoint[Seq[Temperature]] = {
    import com.lunaryorn.weather.codecs.decodeUnitOfMeasureTemperature
    get("temperatures" :: paramOption("unit").as[UnitOfMeasure[Temperature]]) {
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

  val endpoints = getTemperatures :+: postTemperature

  Await.ready(Http.server.serve("127.0.0.1:8080", endpoints.toService))
}
