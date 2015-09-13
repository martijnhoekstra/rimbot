package net.fgsquad

import scalaz.concurrent.Task
import org.jibble.pircbot._

package object rimbot {
  type MsgReceived = PircBot => String => Message => Task[Unit]
  def parseInt(str: String): Option[Int] = try { Some(str.toInt) } catch { case _: Throwable => None }
}