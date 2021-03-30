import $ivy.`com.github.pheymann::investment-library:0.0.0`
import investment._, units._, distributions._

/* In this How-To we try to model a stock investment for a
 * retirement plan. First we have to understand what needs we might
 * have in the future. Our plan is to retire in 40 years (with 65yo) and we roughly plan
 * for this fund to survive for 30 years (until we are 95yo). Assuming we have current spendings 
 * of 1000USD per month for one adult that adds up to 12,000USD per year. Because we
 * only have to cover cost like place to live, food, or leisure activities** in the future, 
 * we can be reasonably sure that 12,000USD buying power will still support as in 40 years.
 * To add some margin of safety we might want to add 20% to that, which makes 14,400USD per
 * year.
 * 
 * To summarize:
 *   - lifetime of this stock investment: 70 years
 *   - purchase or investment phase: first 40 years
 *   - expected inflation protected yearly payout: 14,400USD
 *
 * IMPORTANT: Only investing in stocks may expose you to significant price swings (volatility)
 * during the pension phase. You have to make sure to accomodate for that through other
 * asset classes like bonds or by having a cash safety which can carry you for a longer
 * period of time.
 * 
 * *) If I should find the time I might add an explanation for that decision later.
 * **) If medical care needs to be covered that sum might be significantly higher.
 */


 
/* Starting from year 41 we need to sell off stocks and consume dividends to
 * finance uor life. Specifically we need to gain inflation protected
 * 14,400USD net income from that sell off.
 * 
 * Here, we could also have used `stocks.Distributions.inflationProtectedSaleReturn`.
 */
val orderFlow: OrderDistribution[Year] = (year, balance) => {
  if (year > Year(40)) {
    val compoundedInflation = Inflation.compoundedGain(rate = Percentage.fromInt(2), year)
    val inflatedNetSale     = Capital(14400) * compoundedInflation

    balance.addOrder(Order.NetSale(expectedNet = inflatedNetSale))
  } else {
    // we reinvest our dividends to produce more gains in the long-run
    balance.reinvestUnconsumedProfit
  }
}

/* An all-world ETF like FTSE All World grew on average 5-6% per year over the last decades.
 * Again to add a margin of safety we will assume 5% here. 
 */
val valueGrowth: ReturnDistribution[Year] = year => {
  Percentage.fromInt(5)
}

/* Again looking at FTSE All World dividends might be in the realm of 1%. 
 */
val dividends: ReturnDistribution[Year] = year => {
  Percentage.fromInt(1)
}

/* Let's calculate what we have to invest every year or if our current expectations
 * can be met at all.
 */
val Some((yearlyInvestmentSum, transactions)) = stocks.approximateFixedInvestment(
  investmentLifetime         = Years.forYears(70),
  purchaseTime               = Years.forYears(40),
  initialSharePrice          = Capital(1),
  orderDist                  = orderFlow,
  valueGrowthReturnDist      = valueGrowth,
  dividendDist               = dividends,
  isAcceptedRemainingCapital = remaining => remaining > Capital.Zero && Capital(1000) > remaining,
  taxation                   = Taxation.fifoBasedTaxation(capitalGainsTax = Percentage.fromInt(2))
)

println(s"Invest $yearlyInvestmentSum per year")
println(prettyPrint(transactions))