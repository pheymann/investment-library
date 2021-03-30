import investment.units._
package object investment {

  val Order = core.Order

  def prettyPrint[D <: Date](investment: core.TransactionLedger[D]): String = {
    core.TransactionLedger.toTable(investment)
  }
}