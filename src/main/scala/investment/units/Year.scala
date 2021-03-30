package investment
package units

case class Year(value: Int) extends Date {

  override def compareWith(other: Date): Date.Comparison = {
    other match {
      case year: Year => 
        if (this.value < year.value) {
          Date.Comparison.LessThan
        }
        else if (this.value > year.value) {
          Date.Comparison.MoreThan
        }
        else {
          Date.Comparison.Equals
        }

      case _ => Date.Comparison.Different
    }
  }

  override def toDouble: Double = {
    value.toDouble
  }

  override def toString(): String = {
    value.toString()
  }
}