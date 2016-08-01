package com.lunaryorn.weather.free

import cats.data.Xor
import com.lunaryorn.weather.TemperatureError
import com.lunaryorn.weather.free.TemperatureAction._
import squants.{QuantityRange, Temperature}

class WeatherService(temperatureRange: QuantityRange[Temperature]) {
  def addTemperature(temperature: Temperature)
    : TemperatureAction[Xor[TemperatureError, Temperature]] =
    if (temperatureRange.contains(temperature)) {
      TemperatureAction.store(temperature).map(Xor.right)
    } else {
      // FIXME: Why does pure not execute?
      TemperatureAction.pure(
          Xor.left(
              TemperatureError.TemperatureOutOfBoundsError(temperatureRange)))
    }

  def getTemperatures: TemperatureAction[Seq[Temperature]] = TemperatureAction.getAll
}
