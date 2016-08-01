package com.lunaryorn.weather

import com.twitter.util.Try
import io.circe.{JsonObject, ObjectEncoder}
import io.circe.syntax._
import io.finch.Decode
import squants.thermal.Temperature
import squants.{Temperature, UnitOfMeasure}

object codecs {
  implicit val decodeUnitOfMeasureTemperature: Decode[
      UnitOfMeasure[Temperature]] = Decode.instance { symbol =>
    Try.orThrow(Temperature.units.find(_.symbol == symbol))(() =>
          new IllegalArgumentException(s"Unknown unit symbol: $symbol"))
  }

  implicit val encodeException: ObjectEncoder[Exception] =
    ObjectEncoder.instance[Exception] { exception =>
      JsonObject.empty
        .add("type", exception.getClass.getName.asJson)
        .add("message", exception.getMessage.asJson)
    }
}
