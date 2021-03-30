import $ivy.`com.github.pheymann::investment-library:0.0.0`
import investment._, units._, distributions._

/* A bond is payed back at a specific point in time (maturity) and our
 * investment will cease to exists. How to simulate a continues investment
 * plan, which is rolling over the principal from bond A to bond B when
 * A matures, is shown here.
 */

// We invest a capital of 100 each year.
val orderFlow: OrderDistribution[Year] = (year, balance) => {
  balance.addOrder(Order.Purchase(Capital(100)))
}

// This bond yields 2% annually.
val yieldDistribution: ReturnDistribution[Year] = year => {
  Percentage.fromInt(2)
}

val result = bonds.rollingInvestment[Year](
  investmentLifetime = Years.forYears(20),
  timeToMaturity     = Years.forYears(5),
  principal          = Capital(1),
  orderDist          = orderFlow,
  yieldDist          = yieldDistribution,
  taxation           = Taxation.fifoBasedTaxation(capitalGainsTax = Percentage.fromInt(2))
)

println(prettyPrint(result))