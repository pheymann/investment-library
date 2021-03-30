import $ivy.`com.github.pheymann::investment-library:0.0.0`
import investment._, units._, distributions._

// Buy a number of bonds A with capital 100 every year.
// If that capital is not sufficient to purchase a natural number of bonds
// (1, 2, 3, ..., INT) only the next natural number of bonds will be
// purchased.
val orderFlow: OrderDistribution[Year] = (year, balance) => {
  balance.addOrder(Order.Purchase(Capital(100)))
}

// This bond yields 2% annually.
val yieldDistribution: ReturnDistribution[Year] = year => {
  Percentage.fromInt(2)
}

val result = bonds.investment[Year](
  timeToMaturity = Years.forYears(10),
  principal      = Capital(1),
  orderDist      = orderFlow,
  yieldDist      = yieldDistribution,
  taxation       = Taxation.fifoBasedTaxation(capitalGainsTax = Percentage.fromInt(2))
)

println(prettyPrint(result))