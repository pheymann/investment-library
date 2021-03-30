package investment.core

import investment.units._
import org.scalatest.funsuite.AnyFunSuite

class OrderBalanceSpec extends AnyFunSuite {

  test("unconsumed profits can be reinvested") {
    val unconsumedBalance = OrderBalance(
      Order.None, 
      ProfitBalance(profit = Capital(10), consumedProfit = Capital.Zero)
    )

    assert(unconsumedBalance.reinvestUnconsumedProfit === OrderBalance(
      Order.Purchase(Capital(10)),
      ProfitBalance(profit = Capital(10), consumedProfit = Capital(10)
    )))

    val partiallyConsumedBalance = OrderBalance(
      Order.None, 
      ProfitBalance(profit = Capital(10), consumedProfit = Capital(2))
    )

    assert(partiallyConsumedBalance.reinvestUnconsumedProfit === OrderBalance(
      Order.Purchase(Capital(8)),
      ProfitBalance(profit = Capital(10), consumedProfit = Capital(10)
    )))
  }
}