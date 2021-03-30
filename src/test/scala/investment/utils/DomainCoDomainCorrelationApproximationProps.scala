package investment.utils

import org.scalacheck.{Properties, Prop, Gen}

class DomainCoDomainCorrelationApproximationProps extends Properties("Domain/Co-Domain Correlation Approximation") {

  private final val Epsilon = 0.0001

  property("stops when correlation holds") = Prop.forAll(
    Gen.choose(1.0, 5.0),
    Gen.choose(-100, 100)
  ) { (growthFactor, expected) => 
    val result = DomainCoDomainCorrelationApproximation.approximate[Unit](
      start              = 0,
      growthFactor       = growthFactor,
      distance           = approx => expected - approx,
      isDistanceAccepted = _ < Epsilon,
      f                  = a => (a + 1) -> (),
      maxSteps           = 10000
    )
    
    result.exists {
      case (value, _) => Math.abs(value - expected.toDouble + 1) <= Epsilon
    }
  }
}