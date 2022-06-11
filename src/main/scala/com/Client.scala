package com

import java.net.InetSocketAddress
import akka.actor.{Actor, Props}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import com.MBCommand
import play.api.libs.json._

// create socket address
object Client {
  var prop : Props = _
  def props(host :String, port :Int) = {
    if(prop == null) prop = Props(classOf[Client], new InetSocketAddress(host,port))
    prop
  }
}

class Client(remote: InetSocketAddress) extends Actor{
  import akka.io.Tcp._
  import context.system

  println("Connecting to " +  remote.toString)

  // send connect message to the tcp manager
  val manager = IO(Tcp)
  manager ! Connect(remote)

  override def receive: Receive = {
    case Received(data) => {
      println(data)
    }
    case CommandFailed(con: Connect) =>
      print("Connection failed")
      print(con.failureMessage.toString)
      context stop self
    

    case c @ Connected(remote, local) =>
      print(s"Connection to $remote succeeded")
      val connection = sender
      connection ! Register(self)

      context.become {
        case (command : String, topic : String) =>
        // receive command and topic from Main actor
          val tuples = Seq(("command", command), ("topic", topic))
        
        // prepares object MBCommand to be transformed in JSON (beacause JSON parser can't transform simple object to JSON)
          implicit val commandWrites = new Writes[MBCommand] {
            def writes(mbCommand: MBCommand) = Json.obj(
                "command"  -> mbCommand.command,
                "topic" -> mbCommand.topic
                )
            }
        // transforms MBCommand to JSON
          val commandObject = new MBCommand(command, topic)
          val json = Json.toJson(commandObject)
          println("Sending message: " + json)
          connection ! Write(ByteString(Json.stringify(json)))

        case CommandFailed(w: Write) =>
          println("Failed to write request.")
          println(w.failureMessage.toString)

        case Received(data) =>
          println("Received response: " + data.utf8String)

        case "close" =>
          println("Closing connection")
          connection ! Close

        case _: ConnectionClosed =>
          println("Connection closed by server.")
          context stop self
       }
  }
}