package com.lunaryorn.weather.free

import com.lunaryorn.weather.WeatherRepository
import com.twitter.util.Future
import cats.~>

object TemperatureActionInterpreter {
  import TemperatureActionT._

  def interpretWithRepository(
      repo: WeatherRepository
  ): TemperatureActionT ~> Future =
    new (TemperatureActionT ~> Future) {
      override def apply[A](action: TemperatureActionT[A]): Future[A] =
        action match {
          case GetAll => repo.getTemperatures
          case Store(temperature) => repo.addTemperature(temperature)
        }
    }
}
