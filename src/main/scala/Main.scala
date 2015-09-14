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

object Botrun {
  def main(args: Array[String]) {
    val stream = "the name of the stream"
    val name = "username"
    val auth = "oath token in the form oauth:xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"

    val host = "irc.twitch.tv"
    val port = 6667

    def channel = s"#$stream"

    val setup = new BotSetup(name)

    setup.bot.setMessageDelay(2000)

    setup.bot.setVerbose(true);

    setup.bot.connect(host, port, auth);

    val fg = new FGSquaredHandler

    setup.join(stream, fg.rcv);

  }
}