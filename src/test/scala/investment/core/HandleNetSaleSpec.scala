package investment.core

import investment.units._
import org.scalatest.funsuite.AnyFunSuite
import investment.FiFoCostBasisTaxation

class HandleNetSaleSpec extends AnyFunSuite {

  private implicit val taxation = FiFoCostBasisTaxation[Year](Percentage.fromInt(1))

  test("if no capital is left in an investment the expected sale volume is added as missing capital") {
    val lastTransaction = Transaction(
      Year(0), 
      TransactionResult.None, 
      numberOfAssets  = 0, 
      assetPrice      = Capital(100),
      invested        = Capital(1100),
      netProfit       = Capital.Zero,
      valueGrowthRate = Percentage.fromInt(1)
    )

    val (transaction, updatedAssets) = HandleNetSale(
      Year(1), 
      lastTransaction, 
      assetLedger     = AssetLedger.empty, 
      expectedNetSell = Capital(100),
      netProfit       = Capital(11), 
      valueGrowthRate = Percentage.fromInt(2)
    )

    assert(transaction === Transaction(
      date            = Year(1),
      result          = TransactionResult.Sold.Incomplete(expectedNet = Capital(100), missing = Capital(100)),
      numberOfAssets  = 0,
      assetPrice      = Capital(100 + 2),
      invested        = Capital(1100),
      netProfit       = Capital(11),
      valueGrowthRate = Percentage.fromInt(2)
    ))
    assert(updatedAssets.transactions.length === 0)
  }

  test("if not enough capital is left in an investment the delta is added as missing capital") {
    val record = AssetRecord(Year(0), price = Capital(1), numberOfAssets = 1)
    val assets = AssetLedger.empty[Year]
      .append(record)

    val lastTransaction = Transaction(
      Year(0), 
      TransactionResult.None, 
      numberOfAssets  = 1, 
      assetPrice      = Capital(100),
      invested        = Capital(1),
      netProfit       = Capital.Zero,
      valueGrowthRate = Percentage.fromInt(1)
    )

    val (transaction, updatedAssets) = HandleNetSale(
      Year(1), 
      lastTransaction, 
      assetLedger     = assets, 
      expectedNetSell = Capital(150),
      netProfit       = Capital(11), 
      valueGrowthRate = Percentage.fromInt(2)
    )

    assert(transaction === Transaction(
      date            = Year(1),
      result          = TransactionResult.Sold.Incomplete(expectedNet = Capital(150), missing = Capital.fromDouble(49.01)),
      numberOfAssets  = 0,
      assetPrice      = Capital(100 + 2),
      invested        = Capital(1),
      netProfit       = Capital(11),
      valueGrowthRate = Percentage.fromInt(2)
    ))
    assert(updatedAssets.transactions.length === 0)
  }

  test("if enough capital is left in an investment enough assets are sold to cover at least the expectation") {
    val record = AssetRecord(Year(0), price = Capital(1), numberOfAssets = 1)
    val assets = AssetLedger.empty[Year]
      .append(record)
      .append(record)

    val lastTransaction = Transaction(
      Year(0), 
      TransactionResult.None, 
      numberOfAssets  = 1, 
      assetPrice      = Capital(100),
      invested        = Capital(2),
      netProfit       = Capital.Zero,
      valueGrowthRate = Percentage.fromInt(1)
    )

    val (transaction, updatedAssets) = HandleNetSale(
      Year(1), 
      lastTransaction, 
      assetLedger     = assets, 
      expectedNetSell = Capital(90),
      netProfit       = Capital(11), 
      valueGrowthRate = Percentage.fromInt(2)
    )

    assert(transaction === Transaction(
      date            = Year(1),
      result          = TransactionResult.Sold.Realized(expectedNet = Capital(90), realized = Capital.fromDouble(100.99)),
      numberOfAssets  = 1,
      assetPrice      = Capital(100 + 2),
      invested        = Capital(2),
      netProfit       = Capital(11),
      valueGrowthRate = Percentage.fromInt(2)
    ))
    assert(updatedAssets.transactions.length === 1)
  }
}