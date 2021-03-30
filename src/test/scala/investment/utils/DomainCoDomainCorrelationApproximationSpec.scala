package investment.utils

import org.scalatest.funsuite.AnyFunSuite

class DomainCoDomainCorrelationApproximationSpec extends AnyFunSuite {

  test("non-positive growth factor isn't allows") {
    assertThrows[Bug](DomainCoDomainCorrelationApproximation.approximate[Unit](
      start              = 0,
      growthFactor       = -1.0,
      distance           = approx => 0 - approx,
      isDistanceAccepted = _ < 0.0001,
      f                  = a => (a + 1) -> (),
      maxSteps           = 10000
    ))
  }

  test("max-step limit will stop long running approximations") {
    val result = DomainCoDomainCorrelationApproximation.approximate[Unit](
      start              = 0,
      growthFactor       = 1.0,
      distance           = approx => 10 - approx,
      isDistanceAccepted = _ < 0.0001,
      f                  = a => (a + 1) -> (),
      maxSteps           = 2
    )

    assert(result === None)
  }
}