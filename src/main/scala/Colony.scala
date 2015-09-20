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

case class Colony(val ingame: List[String], val ded: List[String], val queue: Queue[String])

object Colony {
  def showstring(seq: Seq[String]): String = {
    seq.toList match {
      case last :: next :: tail => (next :: tail).mkString(", ") + " and " + last
      case head :: Nil => head
      case _ => "nobody"
    }
  }

  def joinqueue(colony: Colony)(name: String): Either[String, Colony] = {
    if (colony.queue.any(colonist => name == colonist)) Left[String, Colony]("already queued")
    else if (colony.ded.exists(d => name == d)) Left[String, Colony]("already ded")
    else if (colony.ingame.exists(i => name == i)) Left[String, Colony]("already in game")
    else Right[String, Colony](new Colony(colony.ingame, colony.ded, colony.queue.enqueue(name)))
  }

  def show(colony: Colony): List[String] = {
    val ingame = colony.ingame match {
      case Nil => "There are no viewer colonists in game."
      case colonist :: Nil => s"lone colonist $colonist represents chat in this colony."
      case _ => s"colonists ${showstring(colony.ingame)} represent chat in this colony."
    }
    val dead = colony.ded match {
      case Nil => None
      case head :: Nil => Some(s"$head has met their demise.")
      case _ => Some(s"${showstring(colony.ded)} are ded.")
    }
    val totalqueue = colony.queue.toList

    val queueline = totalqueue match {
      case Nil => "Nobody is queued up to join the colony."
      case head :: Nil => s"Only $head is queued to join the colony."
      case _ => s"${showstring(totalqueue)} are waiting in line to join their doom. Eh, the colony."
    }

    ingame :: dead.foldLeft(List(queueline))((agg, dedline) => dedline :: agg)
  }

  def newcolony(oldcolony: Colony) =
    new Colony(Nil, Nil, oldcolony.queue)

  def die(colony: Colony)(colonist: String): Option[Colony] =
    if (colony.ingame.contains(colonist)) {
      Some(new Colony(colony.ingame.filterNot(name => name == colonist), colonist :: colony.ded, colony.queue))
    } else {
      None
    }

  def recruit(colony: Colony): Option[(String, Colony)] =
    colony.queue.dequeue.map { case (colonist, newqueue) => (colonist, new Colony(colonist :: colony.ingame, colony.ded, newqueue)) }

  def apply() = new Colony(Nil, Nil, Queue.empty)
}
