package investment.stocks

import investment.units._
import investment.distributions._
import investment.core._
import investment.Inflation

object Distributions {

  def inflationProtectedSaleReturn(
    inflationRate: Percentage,
    netSale: Capital
  ): OrderDistribution[Year] = (year, balance) => {
    val protectedSale = Inflation.compoundedGain(inflationRate, year) * netSale
  
    balance.addOrder(Order.NetSale(protectedSale))
  }
}