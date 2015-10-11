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

import scalaz.concurrent.Task
import org.jibble.pircbot._

object Handling {
  def asMod[T](bot: PircBot, channel: String, getmods: Task[List[String]]): (List[String], String) => Task[T] => Task[(List[String], Option[T])] =
    (mods, user) => (taction) =>
      if (mods.contains(user)) taction.map(t => (mods, Some(t)))
      else getmods.flatMap(newmods => {
        bot.log(s"new mods are $newmods")
        if (newmods.contains(user))
          taction.map(t => (newmods, Some(t)))
        else
          Task.delay {
            bot.sendMessage(channel, s"$user isn't a mod. It's a very naughty boy/grill!")
            (newmods, None)
          }
      })

}