package investment.core

import investment.units._
import org.scalacheck.Properties
import org.scalacheck.Gen
import org.scalacheck.Prop

object OrderProps extends Properties("Order") {

  val capitalGen     = Gen.posNum[Int].map(Capital(_))
  val purchaseGen    = capitalGen.map(Order.Purchase(_))
  val netSaleGen     = capitalGen.map(Order.NetSale(_))
  val staticOrderGen = Gen.oneOf(Seq(Order.Liquidation, Order.None))
  val orderGen       = Gen.oneOf(purchaseGen, netSaleGen, staticOrderGen)

  property("is commutative") = Prop.forAll(orderGen, orderGen) { (a, b) => 
    a.compose(b) == b.compose(a)
  }

  property("is associative") = Prop.forAll(orderGen, orderGen, orderGen) { (a, b, c) => 
    (a.compose(b)).compose(c) == a.compose(b.compose(c))
  }

  property("None doesn't change other Order") = Prop.forAll(orderGen) { other =>
    Order.None.compose(other) == other
  }

  property("Liquidations have higher priority") = Prop.forAll(orderGen) { other =>
    Order.Liquidation.compose(other) == Order.Liquidation
  }
}