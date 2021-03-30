import $ivy.`com.github.pheymann::investment-library:0.0.0`
import investment._, units._, distributions._

// Buy a number of stocks A worth 100 (no currency specification) every year.
// If the capital is not sufficient to purchase a natural number of stocks
// (1, 2, 3, ..., INT) only the next natural number of stocks will be
// purchased.
//
// Side note: Here, we also sell all our stocks in the last year (Liquidation).
val orderFlow: OrderDistribution[Year] = (year, balance) => {
  if (year == Year(10)) {
    balance.addOrder(Order.Liquidation)
  } else {
    balance.addOrder(Order.Purchase(Capital(100)))
  }
}

// this stock's value growth 6% annually
val valueGrowth: ReturnDistribution[Year] = year => {
  Percentage.fromInt(6)
}

// this stock pays 1% dividends annually
val dividends: ReturnDistribution[Year] = year => {
  Percentage.fromInt(1)
}

// invest for 10 years with an initial stock share price of 1.
val result = stocks.invest(
  investmentLifetime    = Years.forYears(10),
  initialSharePrice     = Capital(1),
  orderDist             = orderFlow,
  valueGrowthReturnDist = valueGrowth,
  dividendDist          = dividends,
  taxation              = Taxation.fifoBasedTaxation(capitalGainsTax = Percentage.fromInt(2))
)

println(prettyPrint(result))