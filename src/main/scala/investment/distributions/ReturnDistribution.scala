package investment.distributions

import investment.units._

trait ReturnDistribution[D <: Date] extends (D => Percentage) {

  def ignoreDateZero(date: D)(implicit dateOps: DateOps[D]): Percentage = {
    if (date == dateOps.zero) {
      Percentage.Zero
    } else {
      this.apply(date)
    }
  }
}

object ReturnDistribution {

  def noReturn[D <: Date]: ReturnDistribution[D] = _ => {
    Percentage.Zero
  }
}