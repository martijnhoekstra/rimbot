package net.fgsquad.rimbot

import scalaz.concurrent.Task
import org.jibble.pircbot._

object Handling {
  def asMod[T](bot: PircBot, channel: String, getmods: Task[List[String]]): (List[String], String) => Task[T] => Task[(List[String], Option[T])] =
    (mods, user) => (taction) =>
      //(mods: List[String], user: String) => (action: =>T) => {
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