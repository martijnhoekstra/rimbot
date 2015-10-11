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

import argonaut._
import Argonaut._

import net.fgsquad.rimbot.Colony
import net.fgsquad.rimbot.DoubleListQueue

object ColonyPickler {
  private[this] case class ColonyPickle(alive: List[String], dead: List[String], queue: List[String])
  private[this] def pkcodec = casecodec3(ColonyPickle.apply, ColonyPickle.unapply)("alive", "dead", "queue")

  implicit def ColonyCodec = pkcodec.xmap(
    pk => Colony(pk.alive, pk.dead, new DoubleListQueue(Nil, pk.queue))
  )(
      col => ColonyPickle(col.ingame, col.ded, col.queue.toList)
    )

  def pickler = FilePickle.jsonpickle[Colony]

}
