package investment.stocks

import investment.units._
import investment.Taxation
import investment.core._
import investment.distributions._

object Stock {

  def computeInvestment[D <: Date: DateOps: Taxation](
    investmentLifetime: TimeRange[D],
    initialSharePrice: Capital,
    orderDist: OrderDistribution[D],
    valueGrowthReturnDist: ReturnDistribution[D],
    dividendDist: ReturnDistribution[D]
  ): TransactionLedger[D] = {
    TransactionLedger.computeInvestment(
      investmentLifetime    = investmentLifetime,
      initialAssetPrice     = initialSharePrice,
      orderDist             = orderDist,
      valueGrowthReturnDist = valueGrowthReturnDist,
      grossProfitDist       = dividendDist
    )
  }
}