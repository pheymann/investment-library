package investment.units

trait Date {

  import Date.Comparison

  def compareWith(that: Date): Comparison

  import Comparison._

  def <(that: Date): Boolean = {
    this.compareWith(that) match {
      case Equals | MoreThan | Different => false
      case _ => true
    }
  }

  def >(that: Date): Boolean = {
    this.compareWith(that) match {
      case Equals | LessThan | Different => false
      case _ => true
    }
  }

  def ==(that: Date): Boolean = {
    this.compareWith(that) match {
      case LessThan | MoreThan | Different => false
      case _ => true
    }
  }

  def <=(that: Date): Boolean = {
    this < that || this == that
  }

  def >=(that: Date): Boolean = {
    this > that || this == that
  }

  def toDouble: Double
}

object Date {

  sealed trait Comparison

  object Comparison {

    case object LessThan extends Comparison
    case object MoreThan extends Comparison
    case object Equals extends Comparison
    case object Different extends Comparison
  }
}

trait DateOps[D <: Date] {

  def zero: D

  def name: String

  def next(date: D): D
}

object DateOps {

  def apply[D <: Date](implicit ops: DateOps[D]): DateOps[D] = ops

  implicit val nextYear = new DateOps[Year]{
    
    override val zero = Year(0)

    override val name = "Year"

    override def next(date: Year): Year = {
      Year(date.value + 1)
    }
  }
}