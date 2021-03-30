package investment

import units._
import core._
import org.scalatest.funsuite.AnyFunSuite
import utils.Bug

class FiFoCostBasisTaxationSpec extends AnyFunSuite {

  private val taxation = FiFoCostBasisTaxation[Year](Percentage.fromInt(1))

  test("When more assets are sold then are in the ledger a bug exception is thrown.") {
    val ledger = AssetLedger.empty[Year]

    assertThrows[Bug](taxation.calculateCostBasis(1, ledger))
  }

  test("When all assets are sold then the cost-basis becomes the total capital of" +
       " the ledger and the ledger itself will be empty.") {
    val purchase = AssetRecord(Year(0), Capital(1), 1)
    val ledger   = AssetLedger.empty[Year]
      .append(purchase)
      .append(purchase)
      .append(purchase)
    
    val (costBasis, emptyLedger) = taxation.calculateCostBasis(3, ledger)

    assert(costBasis === Capital(3))
    assert(emptyLedger === AssetLedger.empty)
  }

  test("A subset of all assets are removed from the ledger to account for the" +
       " number of solf assets.") {
    val ledger = AssetLedger.empty[Year]
      .append(AssetRecord(Year(0), Capital(1), 2))
      .append(AssetRecord(Year(1), Capital(1), 2))
      .append(AssetRecord(Year(2), Capital(2), 1))
    
    val (costBasis, reducedLedger) = taxation.calculateCostBasis(3, ledger)

    assert(costBasis === Capital(3))
    assert(reducedLedger === AssetLedger
      .empty
      .append(AssetRecord(Year(1), Capital(1), 1))
      .append(AssetRecord(Year(2), Capital(2), 1))
    )
  }
}