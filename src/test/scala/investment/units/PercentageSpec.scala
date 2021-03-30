package investment.units

import org.scalatest.funsuite.AnyFunSuite

class PercentageSpec extends AnyFunSuite {

  test("addition with Int") {
    assert(Percentage.fromInt(50) + Percentage.fromInt(100) === Percentage.fromInt(150))
    assert(Percentage.fromInt(0) + Percentage.fromInt(100) === Percentage.fromInt(100))
  }

  test("addition with Double") {
    assert(Percentage.fromDouble(0.5) + Percentage.fromDouble(1.0) === Percentage.fromDouble(1.5))
    assert(Percentage.fromDouble(0.0) + Percentage.fromDouble(1.0) === Percentage.fromDouble(1.0))
  }

  test("addition mixed") {
    assert(Percentage.fromDouble(0.5) + Percentage.fromInt(100) === Percentage.fromInt(150))
    assert(Percentage.fromDouble(0.0) + Percentage.fromInt(100) === Percentage.fromInt(100))
  }

  test("subtraction with Int") {
    assert(Percentage.fromInt(50) - Percentage.fromInt(100) === Percentage.fromInt(-50))
    assert(Percentage.fromInt(0) - Percentage.fromInt(100) === Percentage.fromInt(-100))
  }

  test("subtraction with Double") {
    assert(Percentage.fromDouble(0.5) - Percentage.fromDouble(1.0) === Percentage.fromDouble(-0.5))
    assert(Percentage.fromDouble(0.0) - Percentage.fromDouble(1.0) === Percentage.fromDouble(-1.0))
  }

  test("subtraction mixed") {
    assert(Percentage.fromInt(50) - Percentage.fromDouble(1.0) === Percentage.fromInt(-50))
    assert(Percentage.fromInt(0) - Percentage.fromDouble(1.0) === Percentage.fromInt(-100))
  }

  test("multiplication with Int") {
    assert(Percentage.fromInt(50) * Percentage.fromInt(100) === Percentage.fromInt(50))
    assert(Percentage.fromInt(0) * Percentage.fromInt(100) === Percentage.fromInt(0))
  }

  test("multiplication with Double") {
    assert(Percentage.fromDouble(0.5) * Percentage.fromDouble(1.0) === Percentage.fromDouble(0.5))
    assert(Percentage.fromDouble(0.0) * Percentage.fromDouble(1.0) === Percentage.fromDouble(0.0))
  }

  test("multiplication mixed") {
    assert(Percentage.fromDouble(0.5) * Percentage.fromInt(100) === Percentage.fromDouble(0.5))
    assert(Percentage.fromDouble(0.0) * Percentage.fromInt(100) === Percentage.fromDouble(0.0))
  }
}