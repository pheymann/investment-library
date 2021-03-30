package investment
package units

trait TimeRange[D <: Date] {

  def from: D
  def until: D
  def length: Int

  def isAtBeginning(date: D): Boolean
  def isAtEnd(date: D): Boolean
  def isInRange(date: D): Boolean

  def remaining(date: D): TimeRange[D]
}

trait TimeRangeOps[D <: Date] {

  def buildRange(from: D, span: Int): TimeRange[D]
}

object TimeRangeOps {

  def apply[D <: Date](implicit ops: TimeRangeOps[D]) = ops

  implicit val yearsOps = new TimeRangeOps[Year] {

    override def buildRange(from: Year, span: Int): TimeRange[Year] = {
      Years(from, Year(from.value + span))
    }
  }
}