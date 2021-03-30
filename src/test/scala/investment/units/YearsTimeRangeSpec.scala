package investment.units

import org.scalatest.funsuite.AnyFunSuite

class YearsTimeRangeSpec extends AnyFunSuite {

  test("length: How many years are left?") {
    Years(from = Year(5), until = Year(10)).length === 5
  }

  test("isAtBeginning") {
    val range = Years(from = Year(1), until = Year(10))

    range.isAtBeginning(Year(0)) === false
    range.isAtBeginning(Year(1)) === true
    range.isAtBeginning(Year(11)) === false
  }

  test("isAtEnd") {
    val range = Years(from = Year(0), until = Year(10))

    range.isAtEnd(Year(9)) === false
    range.isAtEnd(Year(10)) === true
    range.isAtEnd(Year(11)) === false
  }

  test("isInRange") {
    val range = Years(from = Year(1), until = Year(10))

    range.isInRange(Year(0)) === false
    range.isInRange(Year(1)) === true
    range.isInRange(Year(2)) === true
    range.isInRange(Year(10)) === true
    range.isInRange(Year(11)) === false
  }

  test("remaining") {
    Years.forYears(10).remaining(Year(5)) === Years(from = Year(5), until = Year(9))
  }
}