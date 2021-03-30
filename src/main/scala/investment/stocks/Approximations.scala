package investment.stocks

import investment.units._
import investment.Taxation
import investment.distributions._
import investment.utils.DomainCoDomainCorrelationApproximation
import investment.core.Order
import investment.core.TransactionLedger

object Approximations {

  def approximateFixedInvestment[D <: Date: DateOps: Taxation](
    investmentLifetime: TimeRange[D],
    purchaseTime: TimeRange[D],
    initialSharePrice: Capital,
    orderDist: OrderDistribution[D],
    valueGrowthReturnDist: ReturnDistribution[D],
    dividendDist: ReturnDistribution[D],
    isAcceptedRemainingCapital: Capital => Boolean,
    approximationLimit: Int = 100
  ): Option[(Capital, TransactionLedger[D])] = {
    val investmentFn: Double => (Double, TransactionLedger[D]) = periodicInvestment => {
      val reinvestingOrderDist = OrderDistribution
        .activeWithin(purchaseTime) { (_, balance) => 
          balance.addOrder(Order.Purchase(Capital.fromDouble(periodicInvestment)))
        }
        .andThen(orderDist)

      val investmentResult = Stock.computeInvestment(
        investmentLifetime    = investmentLifetime,
        initialSharePrice     = initialSharePrice,
        orderDist             = reinvestingOrderDist,
        valueGrowthReturnDist = valueGrowthReturnDist,
        dividendDist          = dividendDist
      )

      val finalGross   = investmentResult.transactions.lastOption.map(_.gross).getOrElse(Capital.Zero)
      val totalMissing = investmentResult.missingCapital
      val diff         = finalGross.toDouble - totalMissing.toDouble

      diff -> investmentResult
    }

    DomainCoDomainCorrelationApproximation
      .approximate(
        start              = 100.0,
        growthFactor       = 1000.0,
        distance           = approx => 0 - approx,
        isDistanceAccepted = rawCapital => isAcceptedRemainingCapital(Capital.fromDouble(rawCapital)),
        f                  = investmentFn,
        maxSteps           = approximationLimit
      )
      .map {
        case (result, ledger) => Capital.fromDouble(result) -> ledger
      }
  }
}