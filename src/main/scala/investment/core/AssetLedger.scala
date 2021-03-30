package investment.core

import investment.units._

// TODO add asset type
case class AssetRecord[D <: Date](date: D, price: Capital, numberOfAssets: Int)

case class AssetLedger[D <: Date](transactions: Seq[AssetRecord[D]]) {

  def totalNumberOfAssets: Int = {
    transactions.map(_.numberOfAssets).sum
  }

  def append(transaction: AssetRecord[D]): AssetLedger[D] = {
    AssetLedger(transactions :+ transaction)
  }
}

object AssetLedger {

  def empty[D <: Date] = AssetLedger[D](Vector.empty)
}