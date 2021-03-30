package investment.core

import investment.utils
import investment.units._

case class ProfitBalance(profit: Capital, consumedProfit: Capital = Capital.Zero) {

  utils.bug(
    profit >= consumedProfit,
    s"Profit consumption is higher than profit itself. Consumed = $consumedProfit, Profit = $profit"
  )

  val unconsumedProfit = profit - consumedProfit

  def totalConsumption: ProfitBalance = {
    copy(consumedProfit = profit)
  }
}

case class OrderBalance(
  order: Order,
  netProfit: ProfitBalance
) {

  def addOrder(other: Order): OrderBalance = {
    copy(order = order.compose(other))
  }

  def reinvestUnconsumedProfit: OrderBalance = {
    addOrder(Order.Purchase(netProfit.unconsumedProfit))
      .copy(netProfit = netProfit.totalConsumption)
  }
}