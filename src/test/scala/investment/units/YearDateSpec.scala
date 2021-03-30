package investment.units

import org.scalatest.funsuite.AnyFunSuite

class YearDateSpec extends AnyFunSuite {

  test("The next year is an increment by one") {
    val ops = DateOps[Year]

    assert(ops.next(ops.zero) === Year(1))
  }
}