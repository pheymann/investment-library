package investment.core

import investment.units._
import org.scalatest.funsuite.AnyFunSuite
import investment.FiFoCostBasisTaxation

class TransactionLedgerSpec extends AnyFunSuite {

  test("Missing capital is the accumulated result over all transactions") {
    assert(TransactionLedger[Year](Vector.empty).missingCapital === Capital.Zero)

    val baseTransaction = Transaction.empty(Year(0), Capital(1))
    val ledger          = TransactionLedger[Year](Vector(
      baseTransaction.copy(result = TransactionResult.None),
      baseTransaction.copy(result = TransactionResult.Purchased(available = Capital(1), used = Capital(1))),
      baseTransaction.copy(result = TransactionResult.Sold.Incomplete(expectedNet = Capital(4), missing = Capital(2))),
      baseTransaction.copy(result = TransactionResult.Sold.Realized(expectedNet = Capital(2), realized = Capital(2))),
      baseTransaction.copy(result = TransactionResult.Sold.Liquidation(realized = Capital(2))),
      baseTransaction.copy(result = TransactionResult.Sold.Incomplete(expectedNet = Capital(2), missing = Capital(1)))
    ))

    assert(ledger.missingCapital === Capital(3))
  }

  private implicit val taxation = FiFoCostBasisTaxation[Year](Percentage.fromInt(1))

  test("compute OrderBalance: ignore Date.Zero") {
    val balance = TransactionLedger.computeOrderBalance[Year](
      date             = Year(0),
      gross            = Capital(100),
      grossProfitsDist = _ => Percentage.fromInt(10),
      orderDist        = (_, balance) => balance.addOrder(Order.Purchase(Capital(100)))
    )

    assert(balance === OrderBalance(
      order     = Order.Purchase(Capital(100)),
      netProfit = ProfitBalance(profit = Capital.Zero, consumedProfit = Capital.Zero)
    ))
  }

  test("compute OrderBalance") {
    val balance = TransactionLedger.computeOrderBalance[Year](
      date             = Year(1),
      gross            = Capital(100),
      grossProfitsDist = _ => Percentage.fromInt(10),
      orderDist        = (_, balance) => balance.addOrder(Order.Purchase(Capital(100)))
    )

    assert(balance === OrderBalance(
      order     = Order.Purchase(Capital(100)),
      netProfit = ProfitBalance(profit = Capital.fromDouble(9.9), consumedProfit = Capital.Zero)
    ))
  }

  test("compute a single Transaction") {
    def runComputeSingleTransaction(order: Order): (Transaction[Year], AssetLedger[Year]) = {
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

      TransactionLedger.computeSingleTransaction[Year](
        now = Year(1),
        lastTransaction,
        assets,
        orderDist             = (_, balance) => balance.addOrder(order),
        valueGrowthReturnDist = _ => Percentage.fromInt(1),
        grossProfitDist       = _ => Percentage.fromInt(1)
      )
    }

    val (purchaseTransaction, purchaseAssets) = runComputeSingleTransaction(Order.Purchase(Capital(110)))

    assert(purchaseTransaction === Transaction(
      date            = Year(1),
      result          = TransactionResult.Purchased(available = Capital(110), used = Capital(101)),
      numberOfAssets  = 4,
      assetPrice      = Capital(101),
      invested        = Capital(1201),
      netProfit       = Capital.fromDouble(3 - 0.03),
      valueGrowthRate = Percentage.fromInt(1)
    ))
    assert(purchaseAssets.totalNumberOfAssets === 4)

    val (netSaleTransaction, netSaleAssets) = runComputeSingleTransaction(Order.NetSale(Capital(100)))

    assert(netSaleTransaction === Transaction(
      date            = Year(1),
      result          = TransactionResult.Sold.Realized(expectedNet = Capital(100), realized = Capital(100)),
      numberOfAssets  = 2,
      assetPrice      = Capital(101),
      invested        = Capital(1100),
      netProfit       = Capital.fromDouble(3 - 0.03),
      valueGrowthRate = Percentage.fromInt(1)
    ))
    assert(netSaleAssets.totalNumberOfAssets === 2)

    val (liquidationTransaction, liquidationAssets) = runComputeSingleTransaction(Order.Liquidation)

    assert(liquidationTransaction === Transaction(
      date            = Year(1),
      result          = TransactionResult.Sold.Liquidation(realized = Capital.fromDouble(300)),
      numberOfAssets  = 0,
      assetPrice      = Capital(101),
      invested        = Capital(1100),
      netProfit       = Capital.fromDouble(3 - 0.03),
      valueGrowthRate = Percentage.fromInt(1)
    ))
    assert(liquidationAssets.totalNumberOfAssets === 0)

    val (noneTransaction, noneAssets) = runComputeSingleTransaction(Order.None)

    assert(noneTransaction === Transaction(
      date            = Year(1),
      result          = TransactionResult.None,
      numberOfAssets  = 3,
      assetPrice      = Capital(101),
      invested        = Capital(1100),
      netProfit       = Capital.fromDouble(3 - 0.03),
      valueGrowthRate = Percentage.fromInt(1)
    ))
    assert(noneAssets.totalNumberOfAssets === 3)
  }

  test("investments are computed up to a certain point in time defined by the TimeRange") {
    val ledger = TransactionLedger.computeInvestment[Year](
      investmentLifetime    = Years.forYears(1),
      initialAssetPrice     = Capital(1),
      orderDist             = (_, balance) => balance.addOrder(Order.Purchase(Capital(2))),
      valueGrowthReturnDist = _ => Percentage.fromInt(1),
      grossProfitDist       = _ => Percentage.fromInt(0)
    )

    assert(ledger.missingCapital === Capital(0))
    assert(ledger.transactions.length === 2)
    assert(ledger.transactions === Vector(
      Transaction(
        date            = Year(0),
        result          = TransactionResult.Purchased(available = Capital(2), used = Capital.fromDouble(2)),
        numberOfAssets  = 2,
        assetPrice      = Capital(1),
        invested        = Capital(2),
        netProfit       = Capital.Zero,
        valueGrowthRate = Percentage.fromInt(0)
      ),
      Transaction(
        date            = Year(1),
        result          = TransactionResult.Purchased(available = Capital(2), used = Capital.fromDouble(1.010)),
        numberOfAssets  = 3,
        assetPrice      = Capital.fromDouble(1.010),
        invested        = Capital.fromDouble(3.010),
        netProfit       = Capital.Zero,
        valueGrowthRate = Percentage.fromInt(1)
      )
    ))
  }
}