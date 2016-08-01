package com.lunaryorn.weather.free

import cats.free.Free
import cats.free.Free.{liftF,pure}
import squants.Temperature

sealed trait TemperatureActionT[T]

object TemperatureActionT {
  case object GetAll extends TemperatureActionT[Seq[Temperature]]
  case class Store(temperature: Temperature)
      extends TemperatureActionT[Temperature]
}

object TemperatureAction {
  type TemperatureAction[T] = Free[TemperatureActionT, T]

  val getAll: TemperatureAction[Seq[Temperature]] = liftF(
      TemperatureActionT.GetAll)

  def store(temperature: Temperature): TemperatureAction[Temperature] =
    liftF(TemperatureActionT.Store(temperature))

  def pure[T](t: T): TemperatureAction[T] = pure(t)
}
