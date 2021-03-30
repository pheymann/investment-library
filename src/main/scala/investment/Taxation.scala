package investment

import units._
import core.AssetLedger
import utils.StrictlyMonotonicIncreaseApproximation

/** Taxation type class. Provides functionality to calculate cost basis and
  * based on that different net/gross transformations.
  */
trait Taxation[D <: Date] {

  def capitalGainsTax: Percentage

  def calculateCostBasis(numberOfSoldAssets: Int, ledger: AssetLedger[D]): (Capital, AssetLedger[D])

  def calculateNetProfit(grossProfit: Capital): Capital = {
    grossProfit * (Percentage.Hundred - capitalGainsTax)
  }

  def calculateCostBasisByGrossSale(
    grossSale: Capital,
    grossAssetPrice: Capital,
    ledger: AssetLedger[D]
  ):(Capital, AssetLedger[D]) = {
    val numberOfSoldAssets = computeNumberOfSoldAssets(grossSale, grossAssetPrice)

    calculateCostBasis(numberOfSoldAssets, ledger)
  }

  def calculateNetSale(grossSale: Capital, costBasis: Capital): Capital = {
    /* the sale produced a capital loss no taxes need to be paid and
     * gross becomes net. 
     */
    if (costBasis > grossSale) {
      grossSale
    } else {
      val capitalGain = grossSale - costBasis
      val taxLoss     = capitalGain * capitalGainsTax

      grossSale - taxLoss
    }
  }

  /* Net sale can be calculated like the following:
   * 
   *  net_sale     = gross_sale - capital_gain * capital_gains_tax
   *  capital_gain = gross_sale - cost_basis
   *  
   * From that follows:
   * 
   *  net_sale = gross_sale - (gross_sale - cost_basis) * capital_gains_tax
   *           = gross_sale * (1 - capital_gains_tax) + cost_basis * capital_gains_tax
   * 
   *  => gross_sale = (net_sale - cost_basis * capital_gains_tax) / (1 - capital_gains_tax)
   * 
   * But with cost_basis(gross_sale) or more specifically cost_basis(number_of_sold_assets).
   * Therefore, we have to approximate the number_of_sold_assets.
   */
  def approximateGrossSale(netSale: Capital, grossAssetPrice: Capital, ledger: AssetLedger[D]): Option[Capital] = {
    val numberOfSoldAssetsOptimization: Double => Double = preciseNumber => {
      val numberOfSoldAssets = Math.ceil(preciseNumber).toInt
      val grossSale          = grossAssetPrice.byFactor(numberOfSoldAssets)
      val (costBasis, _)     = calculateCostBasis(numberOfSoldAssets, ledger)
      val realizedNetSell    = calculateNetSale(grossSale, costBasis)

      realizedNetSell.toDouble
    }

    StrictlyMonotonicIncreaseApproximation
      .approximate(
        start        = 1,
        growthFactor = 1,
        stop         = net => net >= netSale.toDouble,
        f            = numberOfSoldAssetsOptimization,
        maxSteps     = ledger.totalNumberOfAssets
      )
      .map(number => grossAssetPrice.byFactor(Math.ceil(number).toInt))
  }

  private def computeNumberOfSoldAssets(grossSale: Capital, grossAssetPrice: Capital): Int = {
    Math.ceil((grossSale / grossAssetPrice).toDouble).toInt
  }
}

object Taxation {

  def apply[D <: Date](implicit t: Taxation[D]): Taxation[D] = t

  def fifoBasedTaxation[D <: Date](capitalGainsTax: Percentage): FiFoCostBasisTaxation[D] = {
    new FiFoCostBasisTaxation(capitalGainsTax)
  }
}