package investment.utils

import org.scalacheck._

class StrictlyMonotonicIncreaseApproximationProps extends Properties("Strictly Monotonic Increase Approximation") {

  property("stops when function is strictly monotonic") = Prop.forAll(
    Gen.choose(1, 5),
    Gen.choose(1, 100)
  ) { (growthFactor, stopValue) => 
    val result = StrictlyMonotonicIncreaseApproximation.approximate(
      start        = 0,
      growthFactor = growthFactor.toDouble,
      stop         = _.toInt > stopValue,
      f            = a => a + 1,
      maxSteps     = 10000
    )

    val expected = Math.ceil(stopValue.toDouble / growthFactor) * growthFactor
    
    result == Some(expected)
  }
}