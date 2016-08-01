package com.lunaryorn.weather

import io.circe.{Decoder, ObjectEncoder}
import io.circe.generic.semiauto._
import squants.{QuantityRange, Temperature}

sealed trait TemperatureError

object TemperatureError {

  case class TemperatureOutOfBoundsError(range: QuantityRange[Temperature])
      extends TemperatureError

  object Codecs {
    import com.lunaryorn.weather.json._

    implicit val encodeTemperatureError: ObjectEncoder[TemperatureError] =
      deriveEncoder[TemperatureError]

    implicit val decodeTemperatureError: Decoder[TemperatureError] =
      deriveDecoder[TemperatureError]
  }
}
