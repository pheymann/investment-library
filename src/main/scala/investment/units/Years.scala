package investment
package units

case class Years private(from: Year, until: Year) extends TimeRange[Year] {

  val length = until.value - from.value

  override def isAtBeginning(date: Year): Boolean = {
    date.value == from.value
  }

  override def isAtEnd(date: Year): Boolean = {
    date.value == until.value
  }

  override def isInRange(date: Year): Boolean = {
    from.value <= date.value && date.value <= until.value
  }

  override def remaining(date: Year): TimeRange[Year] = {
    this.copy(from = date)
  }
}

object Years {

  def forYears(years: Int): Years = {
    Years(from = Year(0), until = Year(years))
  }
}