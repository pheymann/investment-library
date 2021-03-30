package investment.utils

import scala.annotation.tailrec

/** This approximation assumes a positive correlation between co-domain and
  * domain of `f`, meaning:
  * 
  *   f: a -> b =>
  * 
  *     a0 > a1 => b0 > b1
  *     a0 < a1 => b0 < b1
  *     
  * Under that assumption we can define the following approximation algorithm for `a`:
  * 
  * We try to find `f(a*) = b*` with `b*` being close to our expected `b_expected`: `b* ~= b_expected`.
  * We start with some `a0` and calculate the geometric distance between `b_expected` and `b0`. Depending
  * on the result we do one of two steps:
  * 
  *    
  *   # distance > 0 we add the constant growth factor to `a0`:
  * 
  *   ---*--------*---->
  *      |        |
  *    f(a0)  b_expected
  *               | 
  *   -------*----*---->
  *          |
  *        f(a1)
  * 
  *   # distance < 0 we half the growth factor and remove it from `a0` (switch direction):
  *  
  *   ---*--------*---->                           
  *      |        |     
  *  b_expected  f(a0)     =(switched direction)>             b_expected
  *       |                                                       |
  *   <---*---*---------                             ---------*---*--->
  *           |                                               |
  *         f(a1)                                           f(a1)
  * 
  * Thus, we are getting closer to `b_expected` over time by repeating this procedure, because we are 
  * reducing the growth factor everytime we overreach the goal.
  */
object DomainCoDomainCorrelationApproximation {

  sealed trait Direction {

    def *(value: Double): Double

    def switch: Direction
  }

  object Direction {

    case object Positive extends Direction {

      def *(value: Double): Double = {
        value
      }

      def switch: Direction = Negative
    }

    case object Negative extends Direction {

      def *(value: Double): Double = {
        -value
      }

      def switch: Direction = Positive
    }
  }

  def approximate[Ctx](
    start: Double,
    growthFactor: Double,
    distance: Double => Double,
    isDistanceAccepted: Double => Boolean,
    f: Double => (Double, Ctx),
    maxSteps: Int
  ): Option[(Double, Ctx)] = {
    bug(
      growthFactor > 0.0, 
      s"growthFactor needs to be positive, but was: '$growthFactor'"
    )

    @tailrec
    def loop(approx: Double, growth: Double, direction: Direction, step: Int): Option[(Double, Ctx)] = {
      if (step <= maxSteps) {
        val (result, ctx) = f(approx)
        val dist          = direction * distance(result)

        if (isDistanceAccepted(Math.abs(dist))) {
          Some(approx -> ctx)
        }
        else if (dist < 0) {
          val nextGrowthFactor = growth / 2.0
          val nextApprox       = approx - direction * nextGrowthFactor

          loop(nextApprox, nextGrowthFactor, direction.switch, step + 1)
        } 
        else {
          val nextApprox = approx + direction * growth

          loop(nextApprox, growth, direction, step + 1)
        }
      } else {
        None
      }
    }

    loop(start, growthFactor, Direction.Positive, step = 0)
  }
}