package net.fgsquad.rimbot

import scalaz._
import Scalaz._
import scalaz.std._
import scalaz.std.AllInstances._
import scalaz.scalacheck.ScalazProperties._
import scalaz.scalacheck.ScalazArbitrary._

import scalacheck.ScalaCheckBinding._
import org.scalacheck.{ Arbitrary, Prop, Gen }
import org.scalacheck.Prop.forAll

import org.specs2._

object QueueTest extends SpecLite {

  import QueueOps._

  implicit def ArbitraryQueue[A](implicit aas: Arbitrary[List[A]], bas: Arbitrary[List[A]]): Arbitrary[Queue[A]] =
    for {
      as <- aas
      bs <- bas
    } yield new DoubleListQueue(as, bs)

  implicit def ieq: Equal[Int] = new Equal[Int] {
    def equal(i1: Int, i2: Int) = i1 == i2
  }

  implicit def ishow: Show[Int] = new Show[Int] {}

  implicit def eq: Equal[Queue[Int]] = queueEqual[Int]

  checkAll(monoid.laws[Queue[Int]])
  checkAll(equal.laws[Queue[Int]])
  //checkAll(monad.laws[Queue])

  "enqueue leaves one more element than before at the rear" ! forAll { (q: Queue[Int], i: Int) =>
    {
      val qq = q.enqueue(i)

      def rec(old: Queue[Int], nw: Queue[Int]): Int = {
        (old.dequeue, nw.dequeue) match {
          case (Some((ov, oq)), Some((nv, nq))) => rec(oq, nq)
          case (None, Some((nv, nq))) => nv
          case _ => ???
        }
      }

      rec(q, qq) must_== (i)

    }
  }

  "only dequeue an empty queue leaves None" ! forAll { (q: Queue[Int]) =>
    {
      q.dequeue match {
        case Some(_) => q.isEmpty must_== (false)
        case None => q.isEmpty must_== (true)
      }

    }
  }

}