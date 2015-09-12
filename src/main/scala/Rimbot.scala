import org.jibble.pircbot._
import scalaz.concurrent.Task

class Rimbot(nick: String, joinc: String, moderators: Task[List[String]]) extends PircBot {
  this.setName(nick)

  var colony: Colony = Colony()
  var mods = moderators.run //List.empty[String]

  def parseInt(str: String): Option[Int] = try { Some(str.toInt) } catch { case _: Throwable => None }

  def asMod[T](user: String, channel: String)(action: => T): Task[Option[T]] = 
    //log(s"running action as mod")
    //action
    if(mods.contains(user)) {
      val actionresult = action
      Task.now(Some(actionresult))
    }
    else moderators.map(newmods => {
      log(s"new mods are $newmods")
      mods = newmods
      if(newmods.contains(user)) {
        val actionresult = action
        Some(actionresult)
      }
      else {
        sendMessage(channel, s"$user isn't a mod. It's a very naughty boy/grill!")
        None
      }
    })
  

  def eval[T](action: => Option[T]): Option[T] = {
    action.foreach(x => x)
    action
  }

  override def onMessage(channel: String, sender: String, login: String, hostname: String, message: String): Unit = {
    import Colony._
    val words = message.split(" ").toList
    words match {
      case command :: casualty :: others if command == "!ded" => asMod(sender, channel){
        val casualties = casualty :: others
        val res = casualties.foldLeft((colony, List.empty[String])){ case ((col, mismatch), next) => die(col)(next) match {
          case Some(ncol) => (ncol, mismatch)
          case None => (col, next :: mismatch)
        }}
        if (res._2.isEmpty) sendMessage(channel, s"death has been dealt")
        else sendMessage(channel, res._2.mkString(", ") + " not in colony. There will be no dying outside of the colony")
        colony = res._1
      }.runAsync(x => {x.map(o => eval(o)); Unit})

      case command :: Nil if command == "!ded" => asMod(sender, channel){
        sendMessage(channel, s"who died? I need names!")
      }.runAsync(x => {x.map(o => eval(o)); Unit})

      case command :: colonists if command == "!join" => if (colonists.isEmpty)
        joinqueue(colony)(sender) match {
          case Right(newcolony) => {
            sendMessage(channel, s"$sender joined queue")
            colony = newcolony
          }
          case Left(error) => sendMessage(channel, error)
        }
        else asMod(sender, channel) {
          val (ncolony, results) = colonists.foldLeft((colony, List.empty[(String, Option[String])])) {
            case ((acolony, aresults), colonist) => joinqueue(colony)(colonist) match {
              case Left(error) => (acolony, (colonist, Some(error)) :: aresults)
              case Right(ncol) => (ncol, (colonist, None) :: aresults)
            }
          }
          colony = ncolony
          val joined = results.collect {case (colonist, None) => colonist}
          val errors = results.collect {case (colonist, Some(error)) => (colonist, error)}
          joined match {
            case Nil => Unit
            case head :: next :: tail => sendMessage(channel, (next :: tail).mkString(", ") + s" and $head entered the queue")
            case head :: Nil => sendMessage(channel, s"$head entered the queue")
          }
          if (errors.isEmpty) Unit
          else sendMessage(channel, errors.map(er => s"${er._1} is ${er._2}").mkString(" "))
        }
      
      case command :: params if command == "!recruit"  => asMod(sender, channel){
        val n = params.headOption.flatMap(parseInt).getOrElse(1)

        val (newcolony, recruited) = (1 to n).foldLeft((colony, List.empty[String])){ case ((col: Colony, recruits: List[String]),_) => 
           recruit(col).foldLeft((col, recruits)){ case (_, (np, nc)) => (nc, np :: recruits)}
        }
        colony = newcolony
        val message = recruited match {
          case Nil => "no volunteers available"
          case head :: second :: tail => (second :: tail).mkString(", ") + s" and $head join step up and join the colony"
          case colonist :: Nil => s"$colonist steps up and joins the colony"
        }
        sendMessage(channel, message)
      }.runAsync(x => {x.map(o => eval(o)); Unit})

      case command :: params if command == "!newcolony" => asMod(sender, channel){
        sendMessage(channel, "a new colony has been started. The queue is preserved.")
        colony = newcolony(colony)
      }.runAsync(x => {x.map(o => eval(o)); Unit})
      case command :: params if command == "!reset" => asMod(sender, channel){
        sendMessage(channel, "a new colony has been started, and the queue is cleared.")
        colony = Colony()
      }.runAsync(x => {x.map(o => eval(o)); Unit})
      case command :: params if command == "!showcolony" => {
        sendMessage(channel, show(colony).mkString(" "))
      }
      case command :: params if command == "!rimbot" => asMod(sender, channel){
        if (params == "verbose" :: Nil) asMod(channel, sender) {
          val msg = """I'm Rimbot, and I maintain the queue of people who want to be in the colony.
          |I know the following commands:
          |!join: you join the queue to join the colony
          |!join <names>: (mod command) <names> all join the queue
          |!showcolony: shows the current state of the colony and the queue
          |!recruit <n>: (mod command) the next n persons in line joins the colony
          |!newcolony: (mod command) starts a new colony. The queue is maintained.
          |!reset: (mod command) resets all the things
          |!rimbot: shows this in short
          |!rimbot verbose: (mod command) shows this in full""".stripMargin
          val parts = msg.split("\n")
          parts.foreach(part => sendMessage(channel, part))
        } else {
          val onemsg = "I'm Rimbot. I maintain the queue of people who want to be in the colony. I know the following commands: " + 
          "!join, !join <names> (mod) !showcolony, !recruit (mod), !newcolony (mod), !reset (mod), !rimbot verbose (mod)"
          sendMessage(channel, onemsg)
      }}.runAsync(x => {x.map(o => eval(o)); Unit})
      case _ => {}
    }
  }

}