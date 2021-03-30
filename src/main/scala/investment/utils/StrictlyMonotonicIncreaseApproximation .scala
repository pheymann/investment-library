package investment.utils

import scala.annotation.tailrec

/** Increasing a start value each iteration by a fixed growth factor until a
  * stopping condition is fulfilled or abort after too many steps.
  *
  * This approximation runs under the assumption that `f` is a strictly monotonic
  * increasing function.
  */
object StrictlyMonotonicIncreaseApproximation {

  def approximate(
    start: Double,
    growthFactor: Double,
    stop: Double => Boolean,
    f: Double => Double,
    maxSteps: Int
  ): Option[Double] = {
    bug(
      growthFactor > 0.0, 
      s"This approximation is required to grow on each step, but growth factor '$growthFactor' < 0"
    )

    @tailrec
    def loop(approx: Double, step: Int): Option[Double] = {
      val result = f(approx)

      if (step > maxSteps) {
        None
      }
      else if (stop(result)) {
        Some(approx)
      } else {
        loop(approx + growthFactor, step + 1)
      }
    }

    loop(start, 0)
  }
}