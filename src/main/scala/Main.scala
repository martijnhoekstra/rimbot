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