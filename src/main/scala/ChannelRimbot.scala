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

class ChannelRimbot(name: String, dispatch: collection.mutable.Map[String, MsgReceived]) extends PircBot {
  setName(name)

  override def onMessage(channel: String, sender: String, login: String, hostname: String, message: String): Unit = {
    val msgrcv = dispatch(channel)
    msgrcv(this)(channel)(Message(sender, login, hostname, message)).runAsync(_ => Unit)
  }
}