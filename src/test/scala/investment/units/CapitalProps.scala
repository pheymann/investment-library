package investment.units

import org.scalacheck._
import org.scalacheck.Prop.propBoolean

object CapitalProps extends Properties("Capital") {

  private val smallInt = Gen.choose(0, 100000000)

  property("addition") = Prop.forAll(smallInt, smallInt) { (a, b) =>
    (Capital(a) + Capital(b)).toDouble.toInt == a + b
  }

  property("subtraction") = Prop.forAll(smallInt, smallInt) { (a, b) =>
    (a > b) ==> {
      (Capital(a) - Capital(b)).toDouble.toInt == a - b
    }
  }

  property("multiplication by a factor") = Prop.forAll(smallInt, Gen.choose(0, 10)) { (a, b) =>
    Capital(a).byFactor(b).toDouble.toInt == a * b
  }

  property("division") = Prop.forAll(smallInt, smallInt) { (a, b) =>
    (b != 0) ==> {
      (Capital(a) / Capital(b)).toDouble.toInt == (a / b).toInt
    }
  }

  property("larger than") = Prop.forAll(smallInt, smallInt) { (a, b) =>
    Capital(a) > Capital(b) == a > b
  }

  property("larger equals") = Prop.forAll(smallInt, smallInt) { (a, b) =>
    Capital(a) >= Capital(b) == a >= b
  }

  property("equals") = Prop.forAll(smallInt) { a =>
    Capital(a) == Capital(a)
  }
}