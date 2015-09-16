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

import java.io.PrintWriter
import scala.util.Try
import scala.util.Success
import scala.util.Failure

trait FilePickle[A] {
  def pickle(a: A, file: String): Try[Unit]
  def unpickle(file: String): Option[A]
  def printToFile[A](f: String)(op: PrintWriter => A): Try[A] = {
    val p = new PrintWriter(f)
    try {
      Success(op(p))
    } catch {
      case t: Throwable => Failure(t)
    } finally {
      p.close()
    }
  }
}

object FilePickle {
  import argonaut._
  import Argonaut._

  def jsonpickle[A](implicit ev: CodecJson[A]) = new FilePickle[A] {
    implicit def dec: DecodeJson[A] = ev
    implicit def enc: EncodeJson[A] = ev
    def pickle(a: A, file: String): Try[Unit] = printToFile(file)(printer => printer.write(ev.encode(a).spaces2))
    def unpickle(f: String) = {
      val str = scala.io.Source.fromFile(f, "utf-8").getLines.mkString
      str.decodeOption[A](dec)
    }

  }

}

