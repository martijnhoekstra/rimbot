/*
  This file is part of Rimbot.

  Rimbot is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  Foobar is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
*/

package net.fgsquad.rimbot

case class ChattersResponse(chatter_count: Int, chatters: Chatters)
case class Chatters(moderators: List[String], staff: List[String], admins: List[String], global_mods: List[String], viewers: List[String])

object Chattersclient {

  import org.http4s.Status.NotFound
  import org.http4s.Status.ResponseClass.Successful
  import argonaut.DecodeJson
  import org.http4s.argonaut.jsonOf
  import argonaut._, Argonaut._
  import org.http4s.Uri._
  import scalaz.concurrent.Task

  implicit def ChattersCodec = casecodec5(Chatters.apply, Chatters.unapply)("moderators", "staff", "admins", "global_mods", "viewers")

  implicit def ChattersResponseCodec =
    casecodec2(ChattersResponse.apply, ChattersResponse.unapply)("chatter_count", "chatters")

  implicit val decoder = jsonOf[ChattersResponse]

  def moderators(channel: String): Task[List[String]] = {
    val client = org.http4s.client.blaze.defaultClient

    val chatters = client(uri("http://tmi.twitch.tv/group/user/fgsquared/chatters")).flatMap {
      case Successful(resp) => resp.as[ChattersResponse]
      //case NotFound(resp)   => Task.now("Not Found!!!")
      //case resp             => Task.now("Failed: " + resp.status)
    }

    chatters.map(cr => cr.chatters.moderators)

  }

}