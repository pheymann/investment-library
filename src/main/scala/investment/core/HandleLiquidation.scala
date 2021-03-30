package investment.core

import investment.units._
import investment.Taxation

/** Sell all assets at once and liquidate this investment.
  */
object HandleLiquidation {

  def apply[D <: Date: DateOps: Taxation](
    date: D, 
    lastTransaction: Transaction[D],
    assetLedger: AssetLedger[D],
    netProfit: Capital,
    valueGrowthRate: Percentage
  ): (Transaction[D], AssetLedger[D]) = {
    val valueGrowth     = Percentage.Hundred + valueGrowthRate
    val grownAssetPrice = lastTransaction.assetPrice * valueGrowth
    val grownGross      = lastTransaction.gross * valueGrowth
    val (costBasis, _)  = Taxation[D].calculateCostBasis(lastTransaction.numberOfAssets, assetLedger)
    val net             = Taxation[D].calculateNetSale(grownGross, costBasis)

    val result = TransactionResult.Sold.Liquidation(
      realized = net
    )

    val transaction = Transaction[D](
      date            = date,
      result          = result,
      numberOfAssets  = 0,
      assetPrice      = grownAssetPrice,
      invested        = lastTransaction.invested,
      netProfit       = netProfit,
      valueGrowthRate = valueGrowthRate
    )

    (transaction, AssetLedger.empty)
  }
}