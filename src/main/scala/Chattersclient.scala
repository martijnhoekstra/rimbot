/*
{
"_links": {},
"chatter_count": 5,
"chatters": {
"moderators": [
"fgsquared",
"fgsrimbot",
"heartymarty",
"lex124",
"nightbot"
],
"staff": [],
"admins": [],
"global_mods": [],
"viewers": []
}
}
*/

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