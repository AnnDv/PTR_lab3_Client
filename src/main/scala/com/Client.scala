package com

import java.net.InetSocketAddress
import akka.actor.{Actor, Props}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import Client.{Ping, SendMessage}

object Client {
  var prop : Props = _
  def props(host :String,port :Int) = {
    if(prop == null) prop = Props(classOf[Client], new 
InetSocketAddress(host,port))
    prop
  }

  final case class SendMessage(message: ByteString)
  final case class Ping(message: String)
}

class Client(remote: InetSocketAddress) extends Actor{
  import akka.io.Tcp._
  import context.system

  println("Connecting to " +  remote.toString)

  val manager = IO(Tcp)
  manager ! Connect(remote)

  override def receive: Receive = {
    case CommandFailed(con: Connect) =>
      print("Connection failed")
      print(con.failureMessage.toString)
      context stop self

    case c @ Connected(remote, local) =>
      print(s"Connection to $remote succeeded")
      val connection = sender
      connection ! Register(self)

      context.become {
        case (command, topic) =>
          println("Sending message: " + command)
          connection ! Write(ByteString(command + "," + topic))
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