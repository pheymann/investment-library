package investment.distributions

import investment.units._
import investment.core.OrderBalance

trait OrderDistribution[D <: Date] extends ((D, OrderBalance) => OrderBalance) {

  def andThen(other: OrderDistribution[D]): OrderDistribution[D] = (date, balance) => {
    val thisbalance  = this.apply(date, balance)
    val otherBalance = other.apply(date, thisbalance)

    otherBalance
  }
}

object OrderDistribution {

  def identity[D <: Date]: OrderDistribution[D] = (_, balance) => {
    balance
  }

  def activeFrom[D <: Date](
    from: D
  )(
    orderDist: OrderDistribution[D]
  ): OrderDistribution[D] = (year, balance) => {
    if (from <= year) {
      orderDist(year, balance)
    } else {
      balance
    }
  }

  def activeUntil[D <: Date](
    until: D
  )(
    orderDist: OrderDistribution[D]
  ): OrderDistribution[D] = (year, balance) => {
    if (year <= until) {
      orderDist(year, balance)
    } else {
      balance
    }
  }

  def activeWithin[D <: Date](
    within: TimeRange[D]
  )(
    orderDist: OrderDistribution[D]
  ): OrderDistribution[D] = {
    activeFrom(within.from)(activeUntil(within.until)(orderDist))
  }
}