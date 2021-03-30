package investment

import units._
import core.AssetLedger
import scala.annotation.tailrec

/* Cost-basis calculation based on the First-in First-out (FiFo) principle. Assets
 * are sold in order of their purchase starting with the oldest assets. 
 * 
 * ## Example
 * An invest bought the following assets in the following order (each entry is a single transaction):
 * 
 *  1. 10 shares in Company A for 100USD each
 *  2. 5 shares in Company A for 120USD each
 *  3. 10 shares in Company A for 80USD each
 * 
 * That means he as 25 shares and payed:
 * 
 *  10 * 100 USD + 5 * 120 USD + 10 * 80 USD = 2,400 USD
 * 
 * Now he wants to sell 12 shares and the current price is 110USD totalling 1,320USD. His cost basis
 * is:
 * 
 *  - 10 shares for 100 USD
 *  - 2 shares for 120 USD
 *  => 1,000 USD + 240 USD = 1,240 USD
 * 
 * And his gain:
 * 
 *  1,320 USD - 1,240 USD = 80 USD
 * 
 * On this 80 USD is going to pay capital gains tax which will reduce his gross sale result of
 * 1,320 USD.
 */
case class FiFoCostBasisTaxation[D <: Date](capitalGainsTax: Percentage) extends Taxation[D] {

  override def calculateCostBasis(
    numberOfSoldAssets: Int, 
    ledger: AssetLedger[D]
  ): (Capital, AssetLedger[D]) = {
    @tailrec
    def loop(
      assetsToBeSold: Int, 
      remainingLedger: AssetLedger[D], 
      partialCostBasis: Capital
    ): (Capital, AssetLedger[D]) = {
      remainingLedger.transactions match {
        // transactions with shares are left in this ledger
        case transaction +: tail =>
          val remainingAssetsToBeSold = assetsToBeSold - transaction.numberOfAssets

          // we exactly cover the required number of assets
          if (remainingAssetsToBeSold == 0) {
            val costBasis = transaction.price.byFactor(transaction.numberOfAssets)

            (partialCostBasis + costBasis, AssetLedger(tail))
          }

          /* Selling all assets from this transaction doesn't cover the required
           * the required number of assets. We consume this transaction and try
           * the next one.
           */
          else if (remainingAssetsToBeSold > 0) {
            val costBasis = transaction.price.byFactor(transaction.numberOfAssets)

            loop(remainingAssetsToBeSold, AssetLedger(tail), partialCostBasis + costBasis)
          }

          /* remainingSoldAssets < 0
           * Only a subset of assets in this transaction is required. We sell it and keep
           * the rest in the ledger.
           */
          else {
            val absRemainingAssets = Math.abs(remainingAssetsToBeSold)
            val updatedPurchase    = transaction.copy(numberOfAssets = absRemainingAssets)
            val updatedLegder      = AssetLedger(updatedPurchase +: tail)
            val costBasis          = transaction.price.byFactor(assetsToBeSold)

            (costBasis + partialCostBasis, updatedLegder)
          }

        // IMPOSSIBLE: this case can't be reached, because it is already handled below
        case Nil => (partialCostBasis, remainingLedger)
      }
    }

    val totalNumberOfAssets = ledger.totalNumberOfAssets

    if (totalNumberOfAssets < numberOfSoldAssets) {
      utils.bug(bugMessage(numberOfSoldAssets, totalNumberOfAssets))
    } else {
      loop(numberOfSoldAssets, ledger, Capital.Zero)
    }
  }

  private final def bugMessage(expectedAssetNumber: Int, availableAssetNumber: Int): String = {
    "More assets were sold than are available in the asset ledger. Unable to compute a cost-basis." +
    s"The expected number of assets was $expectedAssetNumber but only $availableAssetNumber were available."
  }
}