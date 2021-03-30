package investment.core

import investment.units._
import org.scalatest.funsuite.AnyFunSuite

class HandlePurchaseSpec extends AnyFunSuite {

  test("invest as much of the available budget as possible") {
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

    val (transaction, updatedAssets) = HandlePurchase(
      Year(1), 
      lastTransaction, 
      assets, 
      availableBudget = Capital(210),
      netProfit = Capital(11), 
      valueGrowthRate = Percentage.fromInt(2)
    )

    assert(transaction === Transaction(
      date            = Year(1),
      result          = TransactionResult.Purchased(available = Capital(210), used = Capital(204)),
      numberOfAssets  = 5,
      assetPrice      = Capital(100 + 2),
      invested        = Capital(1304),
      netProfit       = Capital(11),
      valueGrowthRate = Percentage.fromInt(2)
    ))
    assert(updatedAssets.transactions.length === 4)
    assert(updatedAssets.transactions.last === AssetRecord(Year(1), price = Capital(102), numberOfAssets = 2))
  }
}