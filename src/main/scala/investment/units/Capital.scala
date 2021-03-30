package investment.units

import investment.utils._
import java.{util => ju}

class Capital private(private val value: Long) {

  override def toString(): String = {
    "%.3f".formatLocal(ju.Locale.US, value.toDouble / Capital.Precision)
  }

  override def equals(obj: Any): Boolean = {
    if (obj == null || !obj.isInstanceOf[Capital]) {
      false
    }
    else {
      obj.asInstanceOf[Capital].value == value
    }
  }

  def +(other: Capital): Capital = {
    new Capital(value + other.value)
  }

  def -(other: Capital): Capital = {
    Capital.withoutPrecision(value - other.value)
  }

  def *(percentage: Percentage): Capital = {
    Capital.withoutPrecision((value * percentage.toDouble).toLong)
  }

  def byFactor(factor: Int): Capital = {
    Capital.fromDouble(this.toDouble * factor)
  }

  def /(other: Capital): Percentage = {
    bug(other > Capital.Zero, s"Tried to divide $this with 0 capital.")
    
    Percentage.fromDouble(this.toDouble / other.toDouble)
  }

  def /(percentage: Percentage): Capital = {
    bug(percentage != Percentage.Zero, s"Tried to divide $this by 0%.")

    Capital.withoutPrecision((value / percentage.toDouble).toLong)
  }

  def >(other: Capital): Boolean = {
    value > other.value
  }

  def >=(other: Capital): Boolean = {
    this == other || this > other
  }

  def toDouble: Double = {
    value.toDouble / Capital.Precision
  }

  def toInt: Int = {
    (value / Capital.Precision).toInt
  }

  def removePrecision: Capital = {
    Capital(this.toInt)
  }
}

object Capital {

  val Precision = 1000L
  val Zero      = Capital(0)

  def apply(value: Int): Capital = {
    withoutPrecision(value * Precision)
  }

  def fromDouble(value: Double): Capital = {
    withoutPrecision((value * Precision).toLong)
  }

  private def withoutPrecision(value: Long): Capital = {
    bug(value >= 0, s"Negative capital isn't allowed. Received $value.")

    new Capital(value)
  }
}
