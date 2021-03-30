package investment

import units._
import core._
import org.scalatest.funsuite.AnyFunSuite

class TaxationSpec extends AnyFunSuite {

  // tested on the basis of FiFoTaxation
  private val taxation = FiFoCostBasisTaxation[Year](Percentage.fromInt(10))

  test("Net-profit is directly taxed since it is a gain.") {
    val netProfit = taxation.calculateNetProfit(Capital(10))

    assertCapital(Capital(9), netProfit)
  }

  test("Cost-basis can also be calculated from gross-sale volume by translating that" +
       " into the number of sold assets.") {
    val ledger = AssetLedger.empty[Year]
      .append(AssetRecord(Year(0), Capital(2), 2))
      .append(AssetRecord(Year(1), Capital(2), 2))
    
    // current asset price is higher than ledger prices
    val (costBasisHigher, ledgerHigher) = taxation.calculateCostBasisByGrossSale(
      Capital(2),
      Capital(3),
      ledger
    )

    assert(costBasisHigher === Capital(2))
    assert(ledgerHigher === AssetLedger.empty
      .append(AssetRecord(Year(0), Capital(2), 1))
      .append(AssetRecord(Year(1), Capital(2), 2))
    )

    // current asset price is equal to ledger prices
    val (costBasisEqual, ledgerEqual) = taxation.calculateCostBasisByGrossSale(
      Capital(2),
      Capital(2),
      ledger
    )

    assert(costBasisEqual === Capital(2))
    assert(ledgerEqual === AssetLedger.empty
      .append(AssetRecord(Year(0), Capital(2), 1))
      .append(AssetRecord(Year(1), Capital(2), 2))
    )

    // current asset price is lower than ledger prices
    val (costBasisLower, ledgerLower) = taxation.calculateCostBasisByGrossSale(
      Capital(2),
      Capital(1),
      ledger
    )

    assert(costBasisLower === Capital(4))
    assert(ledgerLower === AssetLedger.empty
      .append(AssetRecord(Year(1), Capital(2), 2))
    )
  }

  test("Net-sale is gross-sale minus capital gains tax.") {
    // gross-sale is larger than or equal to cost-basis
    val netSale = taxation.calculateNetSale(Capital(10), Capital(1))
    assertCapital(Capital.fromDouble(9.1), netSale)

    // gross-sale is lower than cost-basis
    val netEqualsGross = taxation.calculateNetSale(Capital(1), Capital(10))
    assertCapital(netEqualsGross, netEqualsGross)
  }

  test("Gross-sale can be estimated to net-sale by approximating the number of sold" +
       " assets.") {
    val ledger = AssetLedger.empty[Year]
      .append(AssetRecord(Year(0), Capital(2), 2))
      .append(AssetRecord(Year(1), Capital(2), 2))

    // current asset price is higher than ledger prices
    val grossHigher = taxation.approximateGrossSale(
      Capital(1),
      Capital(3),
      ledger
    )

    assertCapital(Capital(3), grossHigher.get)

    // current asset price is equal to ledger prices
    val grossEqual = taxation.approximateGrossSale(
      Capital(1),
      Capital(2),
      ledger
    )

    assertCapital(Capital(2), grossEqual.get)

    // current asset price is lower than ledger prices
    val grossLower = taxation.approximateGrossSale(
      Capital(1),
      Capital(1),
      ledger
    )

    assertCapital(Capital(1), grossLower.get)
  }

  private def assertCapital(expected: Capital, actual: Capital) = {
    assert(expected == actual, s"expected: $expected, actual: $actual")
  }
}