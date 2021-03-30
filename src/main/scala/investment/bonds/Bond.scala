package investment
package bonds

import units._
import core._
import distributions._

object Bond {

  def computeInvestment[D <: Date: DateOps: Taxation](
    timeToMaturity: TimeRange[D],
    principal: Capital,
    orderDist: OrderDistribution[D],
    yieldDist: ReturnDistribution[D],
  ): TransactionLedger[D] = {
    TransactionLedger.computeInvestment(
      investmentLifetime    = timeToMaturity,
      initialAssetPrice     = principal,
      orderDist             = orderDist.andThen(Distributions.bondMaturityBehaviour(timeToMaturity)),
      valueGrowthReturnDist = ReturnDistribution.noReturn,
      grossProfitDist       = yieldDist
    )
  }

  def computeContinuesInvestmentSeries[D <: Date: DateOps: TimeRangeOps: Taxation](
    investmentLifetime: TimeRange[D],
    timeToMaturity: TimeRange[D],
    principal: Capital,
    orderDist: OrderDistribution[D],
    yieldDist: ReturnDistribution[D],
  ): TransactionLedger[D] = {
    val initialInvestment = computeInvestment(
      timeToMaturity  = timeToMaturity,
      principal       = principal,
      orderDist       = orderDist,
      yieldDist       = yieldDist
    )

    // one round is already computed: initialInvestment
    val bondInvestmentRounds = (investmentLifetime.length / timeToMaturity.length) - 1

    val (_, transactions) = (0 until bondInvestmentRounds).foldLeft(timeToMaturity.until -> initialInvestment.transactions) { case ((from, transactions), _) => 
      val realizedPrincipal = TransactionResult.realizedPayout(transactions.last.result)
      val maturity          = TimeRangeOps[D].buildRange(from, span = timeToMaturity.length)
      val nextBondRound     = computeInvestment[D](
        timeToMaturity  = maturity,
        principal       = principal,
        orderDist       = Distributions.principalReinvestment(maturity, realizedPrincipal).andThen(orderDist),
        yieldDist       = yieldDist
      )

      maturity.until -> (transactions ++ nextBondRound.transactions)
    }

    TransactionLedger(transactions)
  }

  object Distributions {

    def bondMaturityBehaviour[D <: Date](timeToMaturity: TimeRange[D]): OrderDistribution[D] = (date, balance) => {
      if (timeToMaturity.isAtEnd(date)) {
        balance.addOrder(Order.Liquidation)
      } else {
        balance
      }
    }

    def principalReinvestment[D <: Date: DateOps](timeToMaturity: TimeRange[D], principal: Capital): OrderDistribution[D] = (date, balance) => {
      if (timeToMaturity.isAtBeginning(date)) {
        balance.addOrder(Order.Purchase(principal))
      } else {
        balance
      }
    }
  }
}