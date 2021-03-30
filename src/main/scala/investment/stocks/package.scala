package investment

import units._
import distributions._
import investment.core.TransactionLedger

package object stocks {
  
  /** With this function you can model and calculate a sequence of
    * investment transactions for a specific stock, which also gives
    * you information about net profits, tax payable, etc.
    *
    * @param investmentLifetime How long is this investment supposed to live?
    * @param initialSharePrice For how long do you plan to invest capital into this investment?
    * @param orderDist How do you plan to purchase stocks or sell them off over time?
    * @param valueGrowthReturnDist Assumed value growth distribution over `investmentLifetime` for this stock.
    * @param dividendDist Assumed dividend payout distribution over `investmentLifetime` for this stock.
    * @param taxation How is this investment taxed? (FiFo, selected, etc)
    * @return transaction sequence stored in a ledger
    */
  def invest[D <: Date: DateOps](
    investmentLifetime: TimeRange[D],
    initialSharePrice: Capital,
    orderDist: OrderDistribution[D],
    valueGrowthReturnDist: ReturnDistribution[D],
    dividendDist: ReturnDistribution[D],
    taxation: Taxation[D]
  ): core.TransactionLedger[D] = {
    stocks.Stock.computeInvestment[D](
      investmentLifetime,
      initialSharePrice,
      orderDist,
      valueGrowthReturnDist,
      dividendDist
    )(DateOps[D], taxation)
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
    * @param purchaseTime For how long do you plan to invest capital into this investment?
    * @param initialSharePrice What is the stock share price when you start this investment?
    * @param orderDist How do you plan to sell-off stocks? Are there any sporadic/lump-sum investments in-between?
    * @param valueGrowthReturnDist Assumed value growth distribution over `investmentLifetime` for this stock.
    * @param dividendDist Assumed dividend payout distribution over `investmentLifetime` for this stock.
    * @param isAcceptedRemainingCapital What amount of capital should be at least/at most/exactly in this investment after `investmentLifetime`?
    * @param taxation How is this investment taxed? (FiFo, selected, etc)
    * @param approximationLimit [Default = 100] How many steps can the approximation do to reach a result before aborting?
    * @return continued, periodic investment sum for `purchaseTime`
    */
  def approximateFixedInvestment[D <: Date: DateOps](
    investmentLifetime: TimeRange[D],
    purchaseTime: TimeRange[D],
    initialSharePrice: Capital,
    orderDist: OrderDistribution[D],
    valueGrowthReturnDist: ReturnDistribution[D],
    dividendDist: ReturnDistribution[D],
    isAcceptedRemainingCapital: Capital => Boolean,
    taxation: Taxation[D],
    approximationLimit: Int = 100
  ): Option[(Capital, TransactionLedger[D])] = {
    Approximations.approximateFixedInvestment(
      investmentLifetime,
      purchaseTime,
      initialSharePrice,
      orderDist,
      valueGrowthReturnDist,
      dividendDist,
      isAcceptedRemainingCapital,
      approximationLimit
    )(DateOps[D], taxation)
  }
}