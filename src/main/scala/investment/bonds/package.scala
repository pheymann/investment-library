package investment

import units._
import distributions._

package object bonds {
 
  /** With this function you can model and calculate a sequence of
    * investment transactions for a specific bond, which also gives
    * you information about net profits, tax payable, etc.
    *
    * @param timeToMaturity How long does this bond exists before it gets payed back?
    * @param principal What is the borrowed sum defined by this bond?
    * @param orderDist How do you plan to purchase bonds or sell them off over time?
    * @param yieldDist Assumed yield (coupon payout) distribution over `timeToMaturity` for this bond. In most cases it should be static.
    * @param taxation How is this investment taxed? (FiFo, selected, etc)
    * @return transaction sequence stored in a ledger
    */
  def investment[D <: Date: DateOps](
    timeToMaturity: TimeRange[D],
    principal: Capital,
    orderDist: OrderDistribution[D],
    yieldDist: ReturnDistribution[D],
    taxation: Taxation[D]
  ): core.TransactionLedger[D] = {
    bonds.Bond.computeInvestment[D](
      timeToMaturity,
      principal,
      orderDist,
      yieldDist
    )(DateOps[D], taxation)
  }

  /** With this function you can model and calculate a sequence of
    * investment transactions for a rolling bond investment. That means
    * immediately reinvesting the principal in a follow-up bond until this
    * investment goes end-of-life.
    *
    * @param investmentLifetime How long is this investment supposed to live?
    * @param timeToMaturity How long does this bond exists before it gets payed back?
    * @param principal What is the borrowed sum defined by this bond?
    * @param orderDist How do you plan to purchase bonds or sell them off over time?
    * @param yieldDist Assumed yield (coupon payout) distribution over `timeToMaturity` for this bond. In most cases it should be static.
    * @param taxation How is this investment taxed? (FiFo, selected, etc)
    * @return transaction sequence stored in a ledger
    */
  def rollingInvestment[D <: Date: DateOps: TimeRangeOps](
    investmentLifetime: TimeRange[D],
    timeToMaturity: TimeRange[D],
    principal: Capital,
    orderDist: OrderDistribution[D],
    yieldDist: ReturnDistribution[D],
    taxation: Taxation[D]
  ): core.TransactionLedger[D] = {
    bonds.Bond.computeContinuesInvestmentSeries[D](
      investmentLifetime,
      timeToMaturity,
      principal,
      orderDist,
      yieldDist
    )(DateOps[D], TimeRangeOps[D], taxation)
  }

  /** With this function you can approximate a fixed purchase sum you have to
    * invest every `Date` interval (say `Year`) to satisfy a defined sell-off
    * behaviour (`orderDist`) and remaining capital constraint after this investment 
    * is considered end-of-life.
    * 
    * One example is a long-term investment plan for retirement. You invest
    * X EUR/USD in an stock ETF every year for the next 40 years and then
    * plan to retrieve Y EUR/USD from it for the next 30 years. With this function
    * you can determine X. Y needs to be derived from your current expenses plus
    * some margin of safety multiplied by an assumed inflation of let's say 2% per
    * year.
    *
    * @param investmentLifetime How long is this investment supposed to live?
    * @param timeToMaturity How long does this bond exists before it gets payed back?
    * @param principal What is the borrowed sum defined by this bond?
    * @param orderDist How do you plan to purchase bonds or sell them off over time?
    * @param yieldDist Assumed yield (coupon payout) distribution over `timeToMaturity` for this bond. In most cases it should be static.
    * @param isAcceptedRemainingCapital What amount of capital should be at least/at most/exactly in this investment after `investmentLifetime`?
    * @param taxation How is this investment taxed? (FiFo, selected, etc)
    * @param approximationLimit [Default = 100] How many steps can the approximation do to reach a result before aborting?
    * @return continued, periodic investment sum for `purchaseTime`
    */
  def approximateFixedInvestment[D <: Date: DateOps: TimeRangeOps](
    investmentLifetime: TimeRange[D],
    purchaseTime: TimeRange[D],
    timeToMaturity: TimeRange[D],
    principal: Capital,
    orderDist: OrderDistribution[D],
    yieldDist: ReturnDistribution[D],
    isAcceptedRemainingCapital: Capital => Boolean,
    taxation: Taxation[D],
    approximationLimit: Int = 100
  ): Option[(Capital, core.TransactionLedger[D])] = {
    Approximations.approximateFixedInvestment(
      investmentLifetime,
      purchaseTime,
      timeToMaturity,
      principal,
      orderDist,
      yieldDist,
      isAcceptedRemainingCapital,
      approximationLimit
    )(DateOps[D], TimeRangeOps[D], taxation)
  }
}