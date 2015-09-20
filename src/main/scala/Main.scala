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

import jline.console.ConsoleReader

object Botrun {
  def getConfig: Option[Config] = Config.readConfig("settings.json")

  def main(args: Array[String]) {
    var arglist = args.toList

    val config = getConfig
    val nomask: Character = null
    val passmask = '*'

    val reader = new ConsoleReader()

    val verbose = config.map(c => c.verbose).getOrElse(true)

    val (name, stream, auth) = {
      val no = config.flatMap(c => c.botname)
      val so = config.flatMap(c => c.channel)
      val ao = config.flatMap(c => c.token)

      (
        no.getOrElse(reader.readLine("bot login name> ", nomask)),
        so.getOrElse(reader.readLine("stream name> ", nomask)),
        ao.getOrElse(reader.readLine("oauth token> ", passmask))
      )
    }

    val fulltoken = "oauth:" + auth

    val host = "irc.twitch.tv"
    val port = 6667

    val channel = s"#$stream"

    val setup = new BotSetup(name)

    setup.bot.setMessageDelay(2000)

    setup.bot.setVerbose(verbose);

    setup.bot.connect(host, port, fulltoken);

    val fg = new FGSquaredHandler

    setup.join(stream, fg.rcv);

    val exit = reader.readLine("press enter to exit")

    setup.bot.disconnect()
    setup.bot.dispose()

  }
}