package com.lunaryorn.weather.free

import cats.data.Validated
import cats.free.Free
import com.lunaryorn.weather.TemperatureValidationError
import squants.Temperature
import cats.free.Free.{liftF, pure}

sealed trait TemperatureActionT[T]

object TemperatureActionT {
  case class Validate(temperature: Temperature)
      extends TemperatureActionT[
          Validated[TemperatureValidationError, Temperature]]
}

object TemperatureAction {
  type TemperatureAction[T] = Free[TemperatureActionT, T]

  def validate(temperature: Temperature)
    : TemperatureAction[Validated[TemperatureValidationError, Temperature]] =
    liftF(TemperatureActionT.Validate(temperature))
}
