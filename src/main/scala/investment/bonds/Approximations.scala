package investment
package bonds

import units._
import utils.DomainCoDomainCorrelationApproximation
import core._
import distributions._

object Approximations {

  def approximateFixedInvestment[D <: Date: DateOps: TimeRangeOps: Taxation](
    investmentLifetime: TimeRange[D],
    purchaseTime: TimeRange[D],
    timeToMaturity: TimeRange[D],
    principal: Capital,
    orderDist: OrderDistribution[D],
    yieldDist: ReturnDistribution[D],
    isAcceptedRemainingCapital: Capital => Boolean,
    approximationLimit: Int = 100
  ): Option[(Capital, TransactionLedger[D])] = {
    val seriesFun: Double => (Double, TransactionLedger[D]) = periodicalInvestment => {
      val reinvestingOrderDist = OrderDistribution
        .activeWithin(purchaseTime) { (_, balance) => 
          balance.addOrder(Order.Purchase(Capital.fromDouble(periodicalInvestment)))
        }
        .andThen(orderDist)

      val investmentResult = Bond.computeContinuesInvestmentSeries[D](
        investmentLifetime = investmentLifetime,
        timeToMaturity     = timeToMaturity,
        principal          = principal,
        orderDist          = reinvestingOrderDist,
        yieldDist          = yieldDist
      )

      val totalNetCoupon = investmentResult.transactions
        .foldLeft[Capital](Capital.Zero)(_ + _.netProfit)

      val netPayout = investmentResult.transactions
        .lastOption
        .map(transaction => TransactionResult.realizedPayout(transaction.result))
        .getOrElse(Capital.Zero)
      
      val totalPayout = netPayout + totalNetCoupon

      totalPayout.toDouble -> investmentResult
    }

    DomainCoDomainCorrelationApproximation
      .approximate(
        start              = 100.0,
        growthFactor       = 1000.0,
        distance           = approx => 0 - approx,
        isDistanceAccepted = rawCapital => isAcceptedRemainingCapital(Capital.fromDouble(rawCapital)),
        f                  = seriesFun,
        maxSteps           = approximationLimit
      )
      .map {
        case (result, ledger) => Capital.fromDouble(result) -> ledger
      }
  }
}