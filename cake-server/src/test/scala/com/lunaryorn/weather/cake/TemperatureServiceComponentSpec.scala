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

package com.lunaryorn.weather.cake

import cats.data.Validated.{Invalid, Valid}
import cats.data.Xor
import com.lunaryorn.weather._
import com.twitter.util.Future
import org.scalatest.{MustMatchers, OneInstancePerTest, WordSpec}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.prop.PropertyChecks
import org.mockito.Mockito._
import org.scalatest.concurrent.JavaFutures
import com.lunaryorn.weather.prop._
import squants.{QuantityRange, Temperature}

class TemperatureServiceComponentSpec
    extends WordSpec
    with MustMatchers
    with MockitoSugar
    with OneInstancePerTest
    with PropertyChecks
    with JavaFutures
    with TemperatureServiceComponentImpl
    with TemperatureValidatorComponent
    with TemperatureRepositoryComponent {

  override val temperatureValidator: TemperatureValidator =
    mock[TemperatureValidator]
  override val temperatureRepository: TemperatureRepository =
    mock[TemperatureRepository]

  "The WeatherServiceComponent" must {
    "provide a WeatherService" which {
      "gets temperatures from the repository" in {
        forAll { temperatures: Seq[Temperature] =>
          when(temperatureRepository.getTemperatures)
            .thenReturn(Future.value(temperatures))

          temperatureService.getTemperatures.toJavaFuture.futureValue mustBe temperatures
        }
      }

      "stores valid temperatures in the repository" in {
        forAll { temperature: Temperature =>
          when(temperatureValidator.validate(temperature))
            .thenReturn(Valid(temperature))
          when(temperatureRepository.addTemperature(temperature))
            .thenReturn(Future.value(temperature))

          temperatureService
            .addTemperature(temperature)
            .toJavaFuture
            .futureValue mustBe Xor.right(temperature)

          verify(temperatureValidator).validate(temperature)
          verify(temperatureRepository).addTemperature(temperature)
        }
      }

      "fails to store invalid temperatures in the repository" in {
        forAll {
          (temperature: Temperature, range: QuantityRange[Temperature]) =>
            val error =
              TemperatureValidationError.TemperatureOutOfBoundsError(range)
            when(temperatureValidator.validate(temperature))
              .thenReturn(Invalid(error))

            temperatureService
              .addTemperature(temperature)
              .toJavaFuture
              .futureValue mustBe Xor.left(
              TemperatureError.InvalidTemperature(error))

            verify(temperatureValidator).validate(temperature)
            verify(temperatureRepository, never()).addTemperature(temperature)
        }
      }
    }
  }
}
