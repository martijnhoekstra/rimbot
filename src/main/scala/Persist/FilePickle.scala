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

package net.fgsquad.rimbot.persist

import java.io.PrintWriter
import scala.util.Try
import scala.util.Success
import scala.util.Failure

trait FilePickle[A] {
  def pickle(a: A, file: String): Try[Unit]
  def unpickle(file: String): Try[A]

  def printToFile[A](f: String)(op: PrintWriter => A): Try[A] = {
    val p = Try { new PrintWriter(f, "utf-8") }
    val res = p.flatMap(pp => Try { op(pp) })
    p.map(pp => pp.close())
    p.isSuccess
    res
  }
}

object FilePickle {
  import argonaut._
  import Argonaut._

  def trydecode[A](str: String, dec: DecodeJson[A]): Try[A] = {
    import scalaz.Validation
    val dres: Validation[String, A] = str.decodeValidation[A](dec)
    dres.fold[Try[A]]((s: String) => Failure(new Exception(s)), a => Success(a))
  }

  def jsonpickle[A](implicit ev: CodecJson[A]) = new FilePickle[A] {
    def pickle(a: A, file: String): Try[Unit] = printToFile(file)(printer => printer.write(ev.encode(a).spaces2))
    def unpickle(f: String): Try[A] = (Try { scala.io.Source.fromFile(f, "utf-8").getLines.mkString }).flatMap(str => trydecode(str, ev))

  }

}

