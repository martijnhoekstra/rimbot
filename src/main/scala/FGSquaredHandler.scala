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

import org.jibble.pircbot._
import scalaz.concurrent.Task
import Colony._

class FGSquaredHandler {

  var colony: Colony = Colony()
  var mods: List[String] = List.empty[String]

  val rcv: MsgReceived = (bot: PircBot) => (channel: String) => {
    def moderators = Chattersclient.moderators(channel.substring(1))

    def reply(msg: String) = bot.sendMessage(channel, msg)

    //def reply(msg: String) = bot.sendMessage("HeartyMarty", msg)

    def asMod[T](user: String) = Handling.asMod[T](bot, channel, moderators)(mods, user)

    def runmod[T](user: String)(action: => T): Task[Option[T]] = asMod(user)(Task.delay(action)).map(t => {
      mods = t._1
      t._2
    })

    (msg: Message) => {
      val words = msg.content.split(" ").toList.map(_.toLowerCase) //case is locale dependent, but the only aim here is to canonize
      val sender = msg.sender
      val task = words match {
        case "!ded" :: casualty :: others => runmod(msg.sender) {
          val casualties = casualty :: others
          val (newcol: Colony, failures: List[String]) = casualties.foldLeft((colony, List.empty[String])) {
            case ((col, mismatch), next) => die(col)(next) match {
              case Some(ncol) => (ncol, mismatch)
              case None => (col, next :: mismatch)
            }
          }

          reply(if (failures.isEmpty) "death has been dealt" else failures.mkString(", ") + " not in colony. There will be no dying outside of the colony")
          colony = newcol
        }

        case "!ded" :: Nil => runmod(msg.sender) {
          reply("who died? I need names!")
        }

        case "!join" :: colonists =>
          if (colonists.isEmpty)
            Task.delay {
              joinqueue(colony)(sender) match {
                case Right(newcolony) => {
                  reply(s"$sender joined queue")
                  colony = newcolony
                }
                case Left(error) => reply(error)
              }
            }
          else runmod(msg.sender) {
            val (ncolony, results) = colonists.foldLeft((colony, List.empty[(String, Option[String])])) {
              case ((acolony, aresults), colonist) => joinqueue(colony)(colonist) match {
                case Left(error) => (acolony, (colonist, Some(error)) :: aresults)
                case Right(ncol) => (ncol, (colonist, None) :: aresults)
              }
            }
            colony = ncolony
            val joined = results.collect { case (colonist, None) => colonist }
            val errors = results.collect { case (colonist, Some(error)) => (colonist, error) }
            joined match {
              case Nil => Unit
              case head :: next :: tail => reply((next :: tail).mkString(", ") + s" and $head entered the queue")
              case head :: Nil => reply(s"$head entered the queue")
            }
            if (errors.isEmpty) Unit
            else reply(errors.map(er => s"${er._1} is ${er._2}").mkString(" "))
          }

        case "!recruit" :: params => runmod(sender) {
          val n = params.headOption.flatMap(parseInt).getOrElse(1)

          val (newcolony, recruited) = (1 to n).foldLeft((colony, List.empty[String])) {
            case ((col: Colony, recruits: List[String]), _) =>
              recruit(col).foldLeft((col, recruits)) { case (_, (np, nc)) => (nc, np :: recruits) }
          }
          colony = newcolony
          val message = recruited match {
            case Nil => "no volunteers available"
            case head :: second :: tail => (second :: tail).mkString(", ") + s" and $head step up and join the colony"
            case colonist :: Nil => s"$colonist steps up and joins the colony"
          }
          reply(message)
        }

        case "!newcolony" :: params => runmod(sender) {
          colony = newcolony(colony)
          reply("a new colony has been started. The queue is preserved.")
        }

        case "!reset" :: params => runmod(sender) {
          reply("a new colony has been started, and the queue is cleared.")
          colony = Colony()
        }

        case "!rimbot" :: params =>
          if (params == "verbose" :: Nil) runmod(sender) {
            val parts = List(
              "I'm Rimbot, and I maintain the queue of people who want to be in the colony.",
              "I'm free software, and you can download the source code at https://github.com/martijnhoekstra/rimbot",
              "I know the following commands:",
              "!join: you join the queue to join the colony",
              "!join <names>: (mod command) <names> all join the queue",
              "!showcolony: shows the current state of the colony and the queue",
              "!recruit <n>: (mod command) the next n persons in line joins the colony",
              "!newcolony: (mod command) starts a new colony. The queue is maintained.",
              "!reset: (mod command) resets all the things",
              "!rimbot: shows this in short",
              "!rimbot verbose: (mod command) shows this in full"
            )
            parts.foreach(part => reply(part))
          }
          else Task.delay {
            reply("I'm Rimbot. I maintain the queue of people who want to be in the colony. I know the following commands: " +
              "!join, !join <names> (mod) !showcolony, !recruit (mod), !newcolony (mod), !reset (mod), !rimbot verbose (mod), !source")
          }

        case "!showcolony" :: params => Task.delay { reply(show(colony).mkString(" ")) }

        case "!source" :: _ => Task.delay {
          val msg = "Rimbot is free software licensed under the AGPL. You can download the source code of the software at https://github.com/martijnhoekstra/rimbot"
          bot.sendMessage(sender, msg)
        }

        case _ => Task.now(Unit)
      }

      task.map(_ => Unit)

    }
  }
}