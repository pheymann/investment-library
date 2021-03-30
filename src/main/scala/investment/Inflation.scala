package investment

import units._

object Inflation {

  def compoundedGain[D <: Date](rate: Percentage, date: Date): Percentage = {
    val growthRate = rate + Percentage.Hundred

    Percentage.fromDouble(Math.pow(growthRate.toDouble, date.toDouble))
  }
}