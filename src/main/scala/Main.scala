object Botrun {
  def main(args: Array[String]) {
    val stream = "fgsquared"
    def channel = s"#$stream"

    val bot = new Rimbot("fgsrimbot", channel, Chattersclient.moderators(stream))
    bot.setMessageDelay(2000)
  
    // Enable debugging output.
    bot.setVerbose(true);

    // Connect to the IRC server.
    bot.connect("irc.twitch.tv", 6667,"oauth:1th9pvbg393c9fw5yhmpo7ect6d8ej");

    //bot.changeNick("HeartyMarty")

    // Join the #pircbot channel.
    bot.joinChannel(channel);
    //await termination

    bot.sendMessage(channel, "Rimbot represent!")

    //bot.sendChannel("#FGSqured", "Rimbot in da house")
  
  }
}