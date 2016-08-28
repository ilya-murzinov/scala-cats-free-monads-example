package com.lunaryorn.weather.free

import cats.data.Validated
import com.lunaryorn.weather.{TemperatureValidationError, TemperatureValidator}
import squants.Temperature

import scala.language.implicitConversions

trait Validate[E, T] {
  def validate(t: T): Validated[E, T]
}

object Validate {
  implicit def fromTemperatureValidator(v: TemperatureValidator)
    : Validate[TemperatureValidationError, Temperature] =
    new Validate[TemperatureValidationError, Temperature] {
      override def validate(
          t: Temperature
      ): Validated[TemperatureValidationError, Temperature] = v.validate(t)
    }
}
