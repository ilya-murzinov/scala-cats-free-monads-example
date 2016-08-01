package com.lunaryorn.weather.free

import cats.data.Xor
import com.lunaryorn.weather.{InMemoryWeatherRepository, TemperatureError}
import com.twitter.finagle.Http
import com.twitter.finagle.http.Status
import com.twitter.util.Await
import io.finch._
import io.finch.circe._
import io.catbird.util._
import squants.{Temperature, UnitOfMeasure}
import squants.thermal.TemperatureConversions._

object FreeServer extends App {
  import com.lunaryorn.weather.codecs.encodeException
  import com.lunaryorn.weather.json._
  import TemperatureError.Codecs._

  val repository = new InMemoryWeatherRepository
  val interpret =
    TemperatureActionInterpreter.interpretWithRepository(repository)
  val weatherService = new WeatherService(
      -100.degreesCelsius to 150.degreesCelsius)

  val postTemperature: Endpoint[Xor[TemperatureError, Temperature]] =
    post("temperatures" :: body.as[Temperature]) { temperature: Temperature =>
      weatherService.addTemperature(temperature).foldMap(interpret).map {
        case Xor.Right(newTemperature) => Ok(Xor.right(newTemperature))
        case Xor.Left(error) =>
          Output.payload(Xor.left(error), Status.BadRequest)
      }
    }

  val getTemperatures: Endpoint[Seq[Temperature]] = {
    import com.lunaryorn.weather.codecs.decodeUnitOfMeasureTemperature
    get("temperatures" :: paramOption("unit").as[UnitOfMeasure[Temperature]]) {
      unit: Option[UnitOfMeasure[Temperature]] =>
        for {
          temperatures <- weatherService.getTemperatures.foldMap(interpret)
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
