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

import org.scalacheck.{Arbitrary, Gen}
import squants.UnitOfMeasure
import squants.thermal.{Kelvin, Temperature}

object prop {
  implicit val arbitraryUnitOfMeasureTemperature: Arbitrary[
    UnitOfMeasure[Temperature]] = Arbitrary(Gen.oneOf(Temperature.units.toSeq))

  implicit val arbitraryTemperature: Arbitrary[Temperature] = Arbitrary(for {
    // Generate a positive Kelvin value and convert it to the target scale,
    // to avoid generating invalid temperatures
    value <- Gen.posNum[Double]
    scale <- Gen.oneOf(Temperature.units.toSeq)
  } yield Kelvin(value).in(scale))

}
