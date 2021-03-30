package investment.core

import investment.units._
import investment.utils
import investment.Taxation

/** Sell as many assets until the expected net sell result is reached or the whole
  * investment is liquidated. 3 scenarios are possible
  * 
  *   1. No capital is left in this investment and therefore no assets can be sold.
  *   2. Some gross capital is left, but it isn't enough to cover the net sell
  *      expectations
  *   3. enough gross capital is left to cover the net sell expectations.
  */
object HandleNetSale {

  def apply[D <: Date: DateOps: Taxation](
    date: D, 
    lastTransaction: Transaction[D],
    assetLedger: AssetLedger[D],
    expectedNetSell: Capital,
    netProfit: Capital,
    valueGrowthRate: Percentage
  ): (Transaction[D], AssetLedger[D]) = {
    val valueGrowth     = Percentage.Hundred + valueGrowthRate
    val grownAssetPrice = lastTransaction.assetPrice * valueGrowth
    
    // no capital is left in this investment
    if (lastTransaction.gross == Capital.Zero) {
      handleNetSellZeroGross(
        date, 
        grownAssetPrice, 
        lastTransaction.invested, 
        expectedNetSell, 
        netProfit, 
        valueGrowthRate
      )
    }
    // we still have gross capital we can spend
    else {
      val grownGross     = lastTransaction.gross * valueGrowth
      val (costBasis, _) = Taxation[D].calculateCostBasis(lastTransaction.numberOfAssets, assetLedger)
      val net            = Taxation[D].calculateNetSale(grownGross, costBasis)

      // we try to sell more than we have in net capital in this investment
      if (expectedNetSell > net) {
        handleNetSellOverNet(
          date, 
          grownAssetPrice, 
          lastTransaction.invested, 
          net, 
          expectedNetSell, 
          netProfit, 
          valueGrowthRate
        )
      } 
      else {
        handleNetSellDefault(
          date, 
          grownAssetPrice,
          lastTransaction.invested,
          assetLedger,
          grownGross, 
          expectedNetSell, 
          netProfit, 
          valueGrowthRate
        )
      }
    }
  }

  private def handleNetSellZeroGross[D <: Date: DateOps](
    date: D,
    grownAssetPrice: Capital,
    lastInvested: Capital,
    expectedNetSell: Capital,
    netProfit: Capital,
    valueGrowthRate: Percentage
  ): (Transaction[D], AssetLedger[D]) = {
    val result = TransactionResult.Sold.Incomplete(
      expectedNet = expectedNetSell,
      missing     = expectedNetSell
    )

    val transaction = Transaction[D](
      date            = date,
      result          = result,
      numberOfAssets  = 0,
      assetPrice      = grownAssetPrice,
      invested        = lastInvested,
      netProfit       = netProfit,
      valueGrowthRate = valueGrowthRate
    )

    (transaction, AssetLedger.empty)
  }

  private def handleNetSellOverNet[D <: Date: DateOps](
    date: D,
    grownAssetPrice: Capital,
    lastInvested: Capital,
    net: Capital,
    expectedNetSell: Capital,
    netProfit: Capital,
    valueGrowthRate: Percentage,
  ): (Transaction[D], AssetLedger[D]) = {
    val missingNet = expectedNetSell - net
    val result     = TransactionResult.Sold.Incomplete(
      expectedNet = expectedNetSell,
      missing     = missingNet
    )

    val transaction = Transaction[D](
      date            = date,
      result          = result,
      numberOfAssets  = 0,
      assetPrice      = grownAssetPrice,
      invested        = lastInvested,
      netProfit       = netProfit,
      valueGrowthRate = valueGrowthRate
    )

    (transaction, AssetLedger.empty)
  }

  private def handleNetSellDefault[D <: Date: DateOps: Taxation](
    date: D,
    grownAssetPrice: Capital,
    lastInvested: Capital,
    assetLedger: AssetLedger[D],
    grownGross: Capital,
    expectedNetSell: Capital,
    netProfit: Capital,
    valueGrowthRate: Percentage,
  ): (Transaction[D], AssetLedger[D]) = {
    val grossSellOpt = Taxation[D].approximateGrossSale(expectedNetSell, grownAssetPrice, assetLedger)
    utils.bug(grossSellOpt.isDefined, s"'Gross from net sell' estimation failed. Net sell is $expectedNetSell.")

    val grossSell                  = grossSellOpt.get
    val (costBasis, updatedLedger) = Taxation[D].calculateCostBasisByGrossSale(grossSell, grownAssetPrice, assetLedger)
    val realizedNetSell            = Taxation[D].calculateNetSale(grossSell, costBasis)
    val result                     = TransactionResult.Sold.Realized(
      expectedNet = expectedNetSell,
      realized    = realizedNetSell
    )

    val transaction = Transaction[D](
      date            = date,
      result          = result,
      numberOfAssets  = updatedLedger.totalNumberOfAssets,
      assetPrice      = grownAssetPrice,
      invested        = lastInvested,
      netProfit       = netProfit,
      valueGrowthRate = valueGrowthRate
    )

    (transaction, updatedLedger)
  }
}