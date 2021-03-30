package investment.core

import investment.units._

object HandleNoTransaction {

  def apply[D <: Date: DateOps](
    date: D,
    lastTransaction: Transaction[D],
    assetLedger: AssetLedger[D],
    netProfit: Capital,
    valueGrowthRate: Percentage
  ): (Transaction[D], AssetLedger[D]) = {
    val valueGrowth     = Percentage.Hundred + valueGrowthRate
    val grownAssetPrice = lastTransaction.assetPrice * valueGrowth

    val transaction = Transaction[D](
      date            = date,
      result          = TransactionResult.None,
      numberOfAssets  = lastTransaction.numberOfAssets,
      assetPrice      = grownAssetPrice,
      invested        = lastTransaction.invested,
      netProfit       = netProfit,
      valueGrowthRate = valueGrowthRate
    )

    (transaction, assetLedger)
  }
}