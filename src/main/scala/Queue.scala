/*
  This file is part of Rimbot.

  Rimbot is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  Rimbot is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with Rimbot.  If not, see <http://www.gnu.org/licenses/>.
*/

package net.fgsquad.rimbot

import scalaz.Monoid
import scalaz.Monad
import scalaz.MonadPlus
import scalaz.Equal
import scala.annotation.tailrec

object QueueOps {
  import scalaz.std.list._
  implicit def queueMonoid[A]: Monoid[Queue[A]] = new Monoid[Queue[A]] {
    def zero = Queue.empty
    def append(q1: Queue[A], q2: => Queue[A]): Queue[A] =
      new DoubleListQueue(q2.toList.reverse, q1.toList)
  }

  implicit def QueueMonad: MonadPlus[Queue] = new MonadPlus[Queue] {
    def point[A](a: => A): Queue[A] = Queue(a)

    def bind[A, B](fa: Queue[A])(f: A => Queue[B]): Queue[B] = fa.dequeue match {
      case None => Queue.empty
      case Some((a, rest)) => {
        @tailrec
        def rec(agg: Queue[B], remain: Queue[A]): Queue[B] = {
          remain.dequeue match {
            case Some((a, q)) => rec(queueMonoid[B].append(agg, f(a)), q)
            case None => agg
          }
        }
        rec(Queue.empty, fa)
      }
    }

    def plus[A](a: Queue[A], b: => Queue[A]): Queue[A] = queueMonoid[A].append(a, b)

    def empty[A]: Queue[A] = Queue.empty

  }

  implicit def queueEqual[A](implicit ev: Equal[A]): Equal[Queue[A]] = new Equal[Queue[A]] {
    import scalaz.std.option._
    import scalaz.std.tuple._

    def equal(q1: Queue[A], q2: Queue[A]) =
      if (q1.isEmpty) q2.isEmpty
      else implicitly[Equal[Option[(A, Queue[A])]]].equal(q1.dequeue, q2.dequeue)

  }
}

trait Queue[+A] {
  def isEmpty: Boolean
  def enqueue[AA >: A](a: AA): Queue[AA]
  def dequeue: Option[(A, Queue[A])]
  def any(predicate: A => Boolean): Boolean
  def toList: List[A]
  override def equals(that: Any): Boolean = {
    if (that.isInstanceOf[Queue[A]]) {
      val other = that.asInstanceOf[Queue[A]]
      this.dequeue == other.dequeue
    } else false
  }
}

class DoubleListQueue[+A](val in: List[A], val out: List[A]) extends Queue[A] {
  def isEmpty = in.isEmpty && out.isEmpty
  def enqueue[AA >: A](a: AA) = new DoubleListQueue(a :: in, out)
  def dequeue: Option[(A, Queue[A])] = (in, out) match {
    case (_, head :: tail) => Some((head, new DoubleListQueue(in, tail)))
    case (Nil, Nil) => None
    case _ => new DoubleListQueue(Nil, in.reverse).dequeue
  }

  def any(predicate: A => Boolean) = in.exists(predicate) || out.exists(predicate)

  def toList = out ::: in.reverse

  override def toString = toList.toString

}

object Queue {
  def empty[A]: Queue[A] = EmptyQueue
  def apply[A](a: A): Queue[A] = new DoubleListQueue(Nil, List(a))
}

object EmptyQueue extends Queue[Nothing] {
  def isEmpty = true
  def enqueue[AA](a: AA) = new DoubleListQueue(Nil, List(a))
  def dequeue = None
  def any(pred: Nothing => Boolean) = false
  def toList = Nil
}
