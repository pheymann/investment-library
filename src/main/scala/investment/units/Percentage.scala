package investment.units

import java.{util => ju}

class Percentage private(private val value: Long) {

  override def toString(): String = {
    "%.3f".formatLocal(ju.Locale.US, value.toDouble / Percentage.Precision)
  }
  
  override def equals(obj: Any): Boolean = {
    if (obj == null || !obj.isInstanceOf[Percentage]) {
      false
    }
    else {
      obj.asInstanceOf[Percentage].value == value
    }
  }

  def +(other: Percentage): Percentage = {
    new Percentage(this.value + other.value)
  }

  def -(other: Percentage): Percentage = {
    new Percentage(this.value - other.value)
  }

  def *(other: Percentage): Percentage = {
    Percentage.fromDouble(this.toDouble * other.toDouble)
  }

  def *(capital: Capital): Capital = {
    capital * this
  }

  def toDouble: Double = {
    value.toDouble / 100.0 / Percentage.Precision
  }
}

object Percentage {

  val Precision = 1000L
  val Hundred   = Percentage.fromInt(100)
  val Zero      = Percentage.fromInt(0)

  /** Percentages are provided as floating point:
    *
    *   100% => 1.0,
    *   0%   => 0.0,
    *   etc
    */
  def fromDouble(raw: Double): Percentage = {
    /* Example for raw = 0.5:
     *
     *   raw * Precision == 50%,
     *   raw * Precision == 50.000% (50,000)
     */
    new Percentage((raw * 100.0 * Precision).toLong)
  }

  /** Percentages are provided as integer:
    *
    *   100 => 100%,
    *   0   => 0%,
    *   etc
    */
  def fromInt(raw: Int): Percentage = {
    /* Example for raw = 50%:
     *
     *   raw * Precision == 50.000% (50,000)
     */
    new Percentage(raw * Precision)
  }
}