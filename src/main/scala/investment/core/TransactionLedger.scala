package investment.core

import investment.units._
import investment.distributions._
import investment.Taxation
import scala.annotation.tailrec

sealed trait TransactionResult

object TransactionResult {

  case class Purchased(available: Capital, used: Capital) extends TransactionResult

  sealed trait Sold extends TransactionResult {
    val realized: Capital
  }

  object Sold {

    case class Realized(expectedNet: Capital, realized: Capital)  extends Sold
    case class Liquidation(realized: Capital)                     extends Sold
    case class Incomplete(expectedNet: Capital, missing: Capital) extends Sold {
      override val realized = expectedNet - missing
    }
  }

  case object None extends TransactionResult

  def realizedPayout(result: TransactionResult): Capital = {
    result match {
      case sold: Sold => sold.realized
      case _          => Capital.Zero
    }
  }
}

case class Transaction[D <: Date](
  date: D,
  result: TransactionResult,
  numberOfAssets: Int,
  assetPrice: Capital,
  invested: Capital,
  netProfit: Capital,
  valueGrowthRate: Percentage
) {

  val gross = assetPrice.byFactor(numberOfAssets)
}

object Transaction {

  def empty[D <: Date](date: D, initialAssetPrice: Capital) = Transaction[D](
    date            = date,
    result          = TransactionResult.None,
    numberOfAssets  = 0,
    assetPrice      = initialAssetPrice,
    invested        = Capital.Zero,
    netProfit       = Capital.Zero,
    valueGrowthRate = Percentage.Zero
  ) 
}

case class TransactionLedger[D <: Date](transactions: Vector[Transaction[D]]) {

  def missingCapital: Capital = {
    transactions.foldLeft[Capital](Capital.Zero) { case (totalMissing, transaction) => 
      transaction.result match {
        case TransactionResult.Sold.Incomplete(_, missing) => totalMissing + missing
        case _                                             => totalMissing
      }
    }
  }
}

object TransactionLedger {

  def computeInvestment[D <: Date: DateOps: Taxation](
    investmentLifetime: TimeRange[D],
    initialAssetPrice: Capital,
    orderDist: OrderDistribution[D],
    valueGrowthReturnDist: ReturnDistribution[D],
    grossProfitDist: ReturnDistribution[D]
  ): TransactionLedger[D] = {
    @tailrec
    def loop(
      lastTransaction: Transaction[D],
      transactionLedger: Vector[Transaction[D]],
      assetLedger: AssetLedger[D]
    ): TransactionLedger[D] = {
      val now = DateOps[D].next(lastTransaction.date)

      if (!investmentLifetime.isInRange(now)) {
        TransactionLedger(transactionLedger)
      }
      else {
        val (transaction, updatedAssetsLedger) = computeSingleTransaction(
          now,
          lastTransaction,
          assetLedger,
          orderDist,
          valueGrowthReturnDist,
          grossProfitDist
        )

        loop(
          transaction,
          transactionLedger :+ transaction,
          updatedAssetsLedger
        )
      }
    }

    val initialTransaction           = Transaction.empty[D](investmentLifetime.from, initialAssetPrice)
    val (transaction, updatedLedger) = computeSingleTransaction(
      initialTransaction.date,
      initialTransaction,
      AssetLedger.empty,
      orderDist,
      valueGrowthReturnDist,
      grossProfitDist
    )

    loop(transaction, Vector(transaction), updatedLedger)
  }

  private[core] def computeSingleTransaction[D <: Date: DateOps: Taxation](
    now: D,
    lastTransaction: Transaction[D],
    assetLedger: AssetLedger[D],
    orderDist: OrderDistribution[D],
    valueGrowthReturnDist: ReturnDistribution[D],
    grossProfitDist: ReturnDistribution[D]
  ): (Transaction[D], AssetLedger[D]) = {
    val balance         = computeOrderBalance(now, lastTransaction.gross, grossProfitDist, orderDist)
    val netProfit       = balance.netProfit.unconsumedProfit
    val valueGrowthRate = valueGrowthReturnDist.ignoreDateZero(now)

    balance.order match {
      case Order.Purchase(capital) => HandlePurchase(now, lastTransaction, assetLedger, capital, netProfit, valueGrowthRate)
      case Order.NetSale(net)      => HandleNetSale(now, lastTransaction, assetLedger, net, netProfit, valueGrowthRate)
      case Order.Liquidation       => HandleLiquidation(now, lastTransaction, assetLedger, netProfit, valueGrowthRate)
      case Order.None              => HandleNoTransaction(now, lastTransaction, assetLedger, netProfit, valueGrowthRate)
    }
  }

  private[core] def computeOrderBalance[D <: Date: DateOps: Taxation](
    date: D, 
    gross: Capital,
    grossProfitsDist: ReturnDistribution[D], 
    orderDist: OrderDistribution[D]
  ): OrderBalance = {
    val grossProfitRate = grossProfitsDist.ignoreDateZero(date)
    val grossProfit     = gross * grossProfitRate
    val netProfit       = Taxation[D].calculateNetProfit(grossProfit)
    val balance         = orderDist(
      date, 
      OrderBalance(
        order     = Order.None,
        netProfit = ProfitBalance(netProfit)
      )
    )

    balance
  }

  private val TableHeader = {
    val columns = Seq(
      "Date",
      "Gross",
      "#Assets",
      "Div/Yield",
      "Growth",
      "Type",
      "Bought",
      "Sold",
      "Missing"
    )

    columns.mkString("\t|\t")
  }

  def toTable[D <: Date](investment: TransactionLedger[D]): String = {
    val rows = investment.transactions
      .map { transaction =>
        val elements = Seq(
          transaction.date.toString(),
          transaction.gross,
          transaction.numberOfAssets,
          transaction.netProfit,
          transaction.valueGrowthRate
        ) ++ resultToTableRow(transaction.result)

        elements.mkString("\t|\t")
      }
      .mkString("\n")

    s"$TableHeader\n${"-" * TableHeader.length()}\n$rows"
  }

  private def resultToTableRow(result: TransactionResult): Seq[String] = {
    val zero = Capital.Zero.toString()

    result match {
      case TransactionResult.None                        => Seq("N ", zero, zero, zero)
      case TransactionResult.Purchased(_, used)          => Seq("P ", used.toString(), zero, zero)
      case TransactionResult.Sold.Realized(_, realized)  => Seq("SR", zero, realized.toString(), zero)
      case TransactionResult.Sold.Liquidation(realized)  => Seq("SL", zero, realized.toString(), zero)
      case incomplete: TransactionResult.Sold.Incomplete => Seq("SI", zero, incomplete.realized.toString(), incomplete.realized.toString())
    }
  }
}