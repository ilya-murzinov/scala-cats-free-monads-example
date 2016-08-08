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

import cats.data.XorT
import com.lunaryorn.weather.{InMemoryTemperatureRepositoryComponentImpl, TemperatureError, TemperatureValidatorComponentImpl}
import com.lunaryorn.weather.json._
import com.twitter.finagle.Http
import com.twitter.util.Await
import io.catbird.util._
import io.finch._
import io.finch.circe._
import squants.UnitOfMeasure
import squants.thermal._

object CakeServer
    extends App
    with WeatherServiceComponentImpl
    with InMemoryTemperatureRepositoryComponentImpl
    with TemperatureValidatorComponentImpl {

  import com.lunaryorn.weather.codecs.encodeException

  val postTemperature: Endpoint[Temperature] =
    post("temperatures" :: body.as[Temperature]) { temperature: Temperature =>
      XorT(temperatureService.addTemperature(temperature))
        .leftMap(TemperatureError.toRequestError)
        .fold(BadRequest, Created)
    }

  val getTemperatures: Endpoint[Seq[Temperature]] = {
    import com.lunaryorn.weather.codecs.decodeTemperatureScale
    get("temperatures" :: paramOption("unit").as[TemperatureScale]) {
      unit: Option[TemperatureScale] =>
        for {
          temperatures <- temperatureService.getTemperatures
        } yield
          Ok(
              unit
                .map(unit => temperatures.map(t => t.in(unit)))
                .getOrElse(temperatures))
    }
  }

  val endpoints = getTemperatures :+: postTemperature

  Await.ready(Http.server.serve("127.0.0.1:8080", endpoints.toService))
}
