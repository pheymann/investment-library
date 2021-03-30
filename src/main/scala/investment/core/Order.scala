package investment.core

import investment.units._

/** An order is the attempt to execute a transaction which will buy or sell
  * assets. 
  */
sealed trait Order {

  def compose(other: Order): Order
}

object Order {

  case object None extends Order {

    override def compose(other: Order): Order = {
      other match {
        case purchase: Purchase => purchase
        case sale: NetSale      => sale
        case Liquidation        => Liquidation
        case Order.None         => this
      }
    }
  }

  case class Purchase(capital: Capital) extends Order {

    override def compose(other: Order): Order = {
      other match {
        case Purchase(additionalCapital) => Purchase(capital + additionalCapital)
        case NetSale(netSale)            => capitalToOrder(netSale, capital)
        case Liquidation                 => Liquidation
        case Order.None                  => this
      }
    }
  }

  case class NetSale(expectedNet: Capital) extends Order {

    override def compose(other: Order): Order = {
      other match {
        case Purchase(purchase)     => capitalToOrder(expectedNet, purchase)
        case NetSale(additionalNet) => NetSale(expectedNet + additionalNet)
        case Liquidation            => Liquidation
        case Order.None             => this
      }
    }
  }

  case object Liquidation extends Order {

    override def compose(other: Order): Order = {
      this
    }
  }

  private def capitalToOrder(sale: Capital, purchase: Capital): Order = {
    if (sale == purchase) {
      Order.None
    }
    else if (sale > purchase) {
      NetSale(sale - purchase)
    }
    else {
      Purchase(purchase - sale)
    }
  }
}
