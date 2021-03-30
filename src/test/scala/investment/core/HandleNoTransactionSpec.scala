package investment.core

import org.scalatest.funsuite.AnyFunSuite

import investment.units._

class HandleNoTransactionSpec extends AnyFunSuite {

  test("investment's only change comes from capital changes themselves") {
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

    val (transaction, updatedAssets) = HandleNoTransaction(
      Year(1), 
      lastTransaction, 
      assets, 
      netProfit = Capital(11), 
      valueGrowthRate = Percentage.fromInt(2)
    )

    assert(transaction === Transaction(
      date            = Year(1),
      result          = TransactionResult.None,
      numberOfAssets  = 3,
      assetPrice      = Capital(100 + 2),
      invested        = Capital(1100),
      netProfit       = Capital(11),
      valueGrowthRate = Percentage.fromInt(2)
    ))
    assert(updatedAssets.totalNumberOfAssets === 3)
  }
}