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

import argonaut._
import Argonaut._
import scala.util.Try

case class Config(botname: Option[String], channel: Option[String], token: Option[String], verbose: Boolean)

object Config {
  import persist.FilePickle

  def readConfig(path: String): Try[Config] = {
    implicit def PersonCodecJson = casecodec4(Config.apply, Config.unapply)("botname", "channel", "token", "verbose")
    FilePickle.jsonpickle[Config].unpickle(path)
  }
}