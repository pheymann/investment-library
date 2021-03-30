package investment.units

import org.scalatest.funsuite.AnyFunSuite
import investment.utils.Bug

class CapitalSpec extends AnyFunSuite {

  test("division by capital") {
    assert(Capital.Zero / Capital(2) === Percentage.Zero)
    assert(Capital(4) / Capital(2) === Percentage.fromInt(200))
    assert(Capital(4) / Capital(4) === Percentage.fromInt(100))
    assert(Capital(4) / Capital(8) === Percentage.fromInt(50))

    assertThrows[Bug](Capital(4) / Capital(0))
  }

  test("division by percentage") {
    assert(Capital.Zero / Percentage.fromInt(100) === Capital.Zero)
    assert(Capital(4) / Percentage.fromInt(100) === Capital(4))
    assert(Capital(4) / Percentage.fromInt(200) === Capital(2))
    assert(Capital(4) / Percentage.fromInt(50) === Capital(8))

    assertThrows[Bug](Capital(4) / Percentage.fromInt(0))
  }

  test("multiplication by percentage") {
    assert(Capital(4) * Percentage.fromInt(100) === Capital(4))
    assert(Capital(4) * Percentage.fromInt(200) === Capital(8))
    assert(Capital(4) * Percentage.fromInt(50) === Capital(2))
    assert(Capital(4) * Percentage.fromInt(0) === Capital.Zero)
  }
}