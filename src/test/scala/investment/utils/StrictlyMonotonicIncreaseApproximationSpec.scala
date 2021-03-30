package investment.utils

import org.scalatest.funsuite.AnyFunSuite

class StrictlyMonotonicIncreaseApproximationSpec extends AnyFunSuite {

  test("non-positive growth factor isn't allows") {
    assertThrows[Bug](StrictlyMonotonicIncreaseApproximation.approximate(
      start        = 0,
      growthFactor = -1.0,
      stop         = _.toInt > 1,
      f            = a => a + 1,
      maxSteps     = 10000
    ))
  }

  test("max-step limit will stop long running approximations") {
    val result = StrictlyMonotonicIncreaseApproximation.approximate(
      start        = 0,
      growthFactor = 1.0,
      stop         = _.toInt < 0,
      f            = a => a + 1,
      maxSteps     = 1
    )

    assert(result === None)
  }
}