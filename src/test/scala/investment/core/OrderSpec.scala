package investment.core

import investment.units._
import org.scalatest.funsuite.AnyFunSuite

class OrderSpec extends AnyFunSuite {

  import Order._

  test("adding Purchases accumulates capital") {
    val composition = Purchase(Capital(10)).compose(Purchase(Capital(5)))

    assert(composition === Purchase(Capital(15)))
  }

  test("adding NetSales accumulates capital") {
    val composition = NetSale(Capital(10)).compose(NetSale(Capital(5)))

    assert(composition === NetSale(Capital(15)))
  }

  test("NetSales are subtracted from Purchases and vis versa") {
    val sale = NetSale(Capital(10))

    assert(sale.compose(Purchase(Capital(10))) === None)
    assert(sale.compose(Purchase(Capital(8))) === NetSale(Capital(2)))
    assert(sale.compose(Purchase(Capital(12))) === Purchase(Capital(2)))
  }
}