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

package com.lunaryorn.weather

import com.twitter.util.Try
import io.circe.syntax._
import io.circe.{Encoder, Json, JsonObject, ObjectEncoder}
import io.finch._
import io.finch.items.RequestItem
import squants.thermal.{Temperature, TemperatureScale}

object codecs {
  implicit val decodeTemperatureScale: Decode[TemperatureScale] = Decode.instance { symbol =>
    for {
      unit <- Try.orThrow(Temperature.units.find(_.symbol == symbol))(() =>
        new IllegalArgumentException(s"Unknown unit symbol: $symbol"))
      scale <- Try(unit.asInstanceOf[TemperatureScale])
    } yield scale
  }

  private object FinchErrorEncoders {
    private implicit val encodeRequestItem: Encoder[RequestItem] =
      Encoder.instance(_.description.asJson)

    implicit val encodeErrorNotPresent: ObjectEncoder[Error.NotPresent] =
      ObjectEncoder.instance { error =>
        JsonObject.empty
          .add("item", error.item.asJson)
          .add("type", "Error.NotPresent".asJson)
      }

    implicit val encodeErrorNotParsed: ObjectEncoder[Error.NotParsed] =
      ObjectEncoder.instance { error =>
        JsonObject.empty
          .add("item", error.item.asJson)
          .add("target", error.targetType.runtimeClass.getSimpleName.asJson)
          .add("type", "Error.NotParsed".asJson)
      }

    implicit val encodeErrorNotValid: ObjectEncoder[Error.NotValid] =
      ObjectEncoder.instance { error =>
        JsonObject.empty
          .add("item", error.item.asJson)
          .add("rule", error.rule.asJson)
          .add("type", "Error.NotValid".asJson)
      }
  }

  private def toRequestError(t: Throwable): Seq[RequestError[_]] = t match {
    case error: RequestError[_] => Seq(error)
    case error: Error.NotPresent =>
      import FinchErrorEncoders._
      Seq(
          RequestError("error.request.notPresent",
                       error.getMessage(),
                       Some(error)))
    case error: Error.NotParsed =>
      import FinchErrorEncoders._
      Seq(
          RequestError("error.request.notParsed",
                       error.getMessage(),
                       Some(error)))
    case error: Error.NotValid =>
      import FinchErrorEncoders._
      Seq(
          RequestError("error.request.notValid",
                       error.getMessage(),
                       Some(error)))
    case error: Error.RequestErrors =>
      error.errors.flatMap(toRequestError)
    case _ => Seq(RequestError("exception", t.getMessage))
  }

  implicit val encodeException: Encoder[Exception] =
    Encoder.instance[Exception] { exception =>
      val errors = toRequestError(exception).map(
          RequestError.Codecs.encodeRequestError(_))
      Json.arr(errors: _*)
    }
}
