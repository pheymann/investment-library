package investment.core

import investment.units._

/** Adds a purchase transaction to the investment series.
  */
object HandlePurchase {

  def apply[D <: Date: DateOps](
    date: D,
    lastTransaction: Transaction[D],
    assetLedger: AssetLedger[D],
    availableBudget: Capital,
    netProfit: Capital,
    valueGrowthRate: Percentage
  ): (Transaction[D], AssetLedger[D]) = {
    val valueGrowth           = Percentage.Hundred + valueGrowthRate
    val grownAssetPrice       = lastTransaction.assetPrice * valueGrowth

    val numberOfNewAsset      = numberOfPurchasedAssets(availableBudget, grownAssetPrice)
    val priceAdjustedPurchase = computeAssetPriceAdjustedPurchase(numberOfNewAsset, grownAssetPrice)
    val updatedInvested       = lastTransaction.invested + priceAdjustedPurchase

    val result = TransactionResult.Purchased(
      available = availableBudget,
      used = priceAdjustedPurchase
    )
    
    val transaction = Transaction[D](
      date            = date,
      result          = result,
      numberOfAssets  = lastTransaction.numberOfAssets + numberOfNewAsset,
      assetPrice      = grownAssetPrice,
      invested        = updatedInvested,
      netProfit       = netProfit,
      valueGrowthRate = valueGrowthRate
    )

    val purchase = AssetRecord(date, grownAssetPrice, numberOfNewAsset)

    (transaction, assetLedger.append(purchase))
  }

  /** The amount of available gross investment capital might not buy
    * an exact number of assets for a given price. Take the following
    * example:
    * 
    *   budget:      200 USD
    *   Asset price: 60 USD
    * 
    * We can buy 3 assets for 180 USD in total, but didn't consume the full
    * 200 USD available, or in other words we couldn't add the full budget 
    * to our investment. Therefore, we have to adjust the budget to a level
    * which can be consume complete. In this case 180 USD.
    */
  private def computeAssetPriceAdjustedPurchase[D <: Date](
    numberOfPurchasedAssets: Int, 
    grossAssetPrice: Capital
  ): Capital = {
    grossAssetPrice.byFactor(numberOfPurchasedAssets)
  }

  private def numberOfPurchasedAssets[D <: Date](
    budget: Capital, 
    grossAssetPrice: Capital
  ): Int = {
    (budget / grossAssetPrice).toDouble.toInt
  }
}