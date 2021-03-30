package investment.distributions

import investment.units._
import investment.core._
import org.scalatest.funsuite.AnyFunSuite

object OrderDistributionSpec extends AnyFunSuite {

  test("activeFrom: Only activates an OrderDistribution from a given date onwards") {
    val dist = OrderDistribution.activeFrom(Year(5))(testDistribution)

    dist(Year(0), emptyBalance).order === Order.None
    dist(Year(4), emptyBalance).order === Order.None
    dist(Year(5), emptyBalance).order === Order.Purchase(Capital(100))
    dist(Year(6), emptyBalance).order === Order.Purchase(Capital(100))
    dist(Year(1000), emptyBalance).order === Order.Purchase(Capital(100))
  }

  test("activeUntil: Only activates an OrderDistribution until a given date in time") {
    val dist = OrderDistribution.activeUntil(Year(5))(testDistribution)

    dist(Year(0), emptyBalance).order === Order.Purchase(Capital(100))
    dist(Year(4), emptyBalance).order === Order.Purchase(Capital(100))
    dist(Year(5), emptyBalance).order === Order.Purchase(Capital(100))
    dist(Year(6), emptyBalance).order === Order.None
    dist(Year(1000), emptyBalance).order === Order.None
  }

  test("activeWithin: Only activates an OrderDistribution within a TimeRange") {
    val dist = OrderDistribution.activeWithin(Years.forYears(5))(testDistribution)

    dist(Year(0), emptyBalance).order === Order.Purchase(Capital(100))
    dist(Year(5), emptyBalance).order === Order.Purchase(Capital(100))
    dist(Year(6), emptyBalance).order === Order.None
    dist(Year(1000), emptyBalance).order === Order.None
  }

  private val emptyBalance = OrderBalance(Order.None, ProfitBalance(Capital.Zero))

  private val testDistribution: OrderDistribution[Year] = (_, balance) => {
    balance.addOrder(Order.Purchase(Capital(100)))
  }
}