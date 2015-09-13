package net.fgsquad.rimbot

import org.jibble.pircbot._

case class Message(sender: String, login: String, hostname: String, content: String)

class BotSetup(name: String) {
  val dispatch: collection.mutable.Map[String, MsgReceived] = collection.mutable.Map[String, MsgReceived]()

  val bot = new ChannelRimbot(name, dispatch)

  def join(stream: String, rcv: MsgReceived) = {
    val channel = s"#$stream"
    dispatch += ((channel, rcv))
    bot.joinChannel(channel);
    bot.sendMessage(channel, "Rimbot represent!")
  }

  def part(stream: String) = {
    val channel = s"#$stream"
    dispatch -= channel
    bot.partChannel(channel)
  }
}

class ChannelRimbot(name: String, dispatch: collection.mutable.Map[String, MsgReceived]) extends PircBot {
  setName(name)

  override def onMessage(channel: String, sender: String, login: String, hostname: String, message: String): Unit = {
    val msgrcv = dispatch(channel)
    msgrcv(this)(channel)(Message(sender, login, hostname, message)).runAsync(_ => Unit)
  }
}