package investment

import investment.units._
import org.scalatest.funsuite.AnyFunSuite
import investment.distributions.OrderDistribution
import investment.distributions.ReturnDistribution

class BugRegressionSpec extends AnyFunSuite {

  test("Taxation.approximateGrossSale: preciseAssetNumber cannot be higher than the total number of assets") {
    implicit val fifoCostBasisTaxation = FiFoCostBasisTaxation[Year](Percentage.fromInt(25))

    stocks.Approximations.approximateFixedInvestment[Year](
      investmentLifetime = Years.forYears(70),
      purchaseTime       = Years.forYears(40),
      initialSharePrice  = Capital(1),
      orderDist          = 
        OrderDistribution
          .activeFrom(Year(40))(
            stocks.Distributions.inflationProtectedSaleReturn(
              inflationRate = Percentage.fromDouble(0.02),
              netSale = Capital(36000)
            )
          ),
      valueGrowthReturnDist = _ => Percentage.fromDouble(0.07),
      dividendDist          = ReturnDistribution.noReturn,
      isAcceptedRemainingCapital = capital =>  Capital(1000) > capital
    ).get

    succeed
  }
}