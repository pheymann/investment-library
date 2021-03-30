package investment.core

import org.scalatest.funsuite.AnyFunSuite
import investment.units._
import investment.FiFoCostBasisTaxation

class HandleLiquidationSpec extends AnyFunSuite {

  private implicit val taxation = FiFoCostBasisTaxation[Year](Percentage.fromInt(1))
  
  test("when executed no capital is left") {
    val record = AssetRecord(Year(0), price = Capital(1), numberOfAssets = 1)
    val assets = AssetLedger.empty[Year]
      .append(record)
      .append(record)
      .append(record)
    
    val lastTransaction = Transaction(
      Year(0), 
      TransactionResult.None, 
      assets.totalNumberOfAssets, 
      assetPrice      = Capital(100),
      invested        = Capital(1100),
      netProfit       = Capital(10),
      valueGrowthRate = Percentage.fromInt(1)
    )

    val (transaction, updatedAssets) = HandleLiquidation(
      Year(1), 
      lastTransaction, 
      assets, 
      netProfit       = Capital(11), 
      valueGrowthRate = Percentage.fromInt(2)
    )

    assert(transaction === Transaction(
      date            = Year(1),
      result          = TransactionResult.Sold.Liquidation(Capital.fromDouble(300 + 3 - 0.03)),
      numberOfAssets  = 0,
      assetPrice      = Capital(100 + 2),
      invested        = Capital(1100),
      netProfit       = Capital(11),
      valueGrowthRate = Percentage.fromInt(2)
    ))
    assert(updatedAssets.totalNumberOfAssets === 0)
  }
}