package investment.units

import org.scalacheck.{Gen, Prop, Properties}
import org.scalacheck.Prop.propBoolean

object PercentageProps extends Properties("Percentage") {

  private val DoubleLowerBound = 1.0 / Percentage.Precision
  private val smallInt         = Gen.choose(-100000000, 100000000)
  private val smallDouble      = Gen.choose(-100000000.0, 100000000.0)

  property("toDouble") = Prop.forAll(smallDouble) { a: Double =>
    Math.abs(Percentage.fromDouble(a).toDouble - a) <= DoubleLowerBound
  }

  property("Integer addition / subtraction") = Prop.forAll(smallInt, smallInt) { (a, b) =>
    Percentage.fromInt(a) + Percentage.fromInt(b) - Percentage.fromInt(b) == Percentage.fromInt(a)
  }

  property("Double addition / subtraction") = Prop.forAll(smallDouble, smallDouble) { (a, b) =>
    Percentage.fromDouble(a) + Percentage.fromDouble(b) - Percentage.fromDouble(b) == Percentage.fromDouble(a)
  }

  property("equals - Int") = Prop.forAll(smallInt) { a =>
    Percentage.fromInt(a) == Percentage.fromInt(a)
  }

  property("equals - Int") = Prop.forAll(smallDouble) { a =>
    Percentage.fromDouble(a) == Percentage.fromDouble(a)
  }
}
