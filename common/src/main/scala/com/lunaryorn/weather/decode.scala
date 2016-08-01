package com.lunaryorn.weather

import com.twitter.util.Try
import io.finch.Decode
import squants.thermal.Temperature
import squants.{Temperature, UnitOfMeasure}

object decode {
  implicit val decodeUnitOfMeasureTemperature: Decode[
      UnitOfMeasure[Temperature]] = Decode.instance { symbol =>
    Try.orThrow(Temperature.units.find(_.symbol == symbol))(() =>
          new IllegalArgumentException(s"Unknown unit symbol: $symbol"))
  }
}
