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

import io.circe.{Encoder, Json, JsonObject, ObjectEncoder}
import io.circe.syntax._

class RequestError[T](
    val kind: String,
    message: String,
    val cause: Option[T]
)(implicit val causeEncoder: Encoder[T])
    extends Exception(message)

object RequestError {

  def apply[T](
      kind: String,
      message: String,
      cause: Option[T] = None
  )(implicit causeEncoder: Encoder[T]) =
    new RequestError[T](kind, message, cause)(causeEncoder)

  object Codecs {
    def encodeRequestError[T]: ObjectEncoder[RequestError[T]] =
      ObjectEncoder.instance { error =>
        implicit val c = error.causeEncoder
        JsonObject.empty
        // Guard against null
          .add("kind", Option(error.kind).asJson)
          .add("message", Option(error.getMessage).asJson)
          .add("cause", error.cause.asJson)
      }
  }
}
