package investment

package object utils {

  case class Bug(msg: String) extends Exception(msg)

  def bug[A](msg: String): A = {
    throw new Bug(s"[BUG] $msg")
  }

  def bug(cond: Boolean, msg: String): Unit = {
    if (!cond) {
      bug(msg)
    }
  }
}
