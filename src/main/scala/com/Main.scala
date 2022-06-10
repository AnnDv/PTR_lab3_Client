package com

import java.net.InetSocketAddress

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.util.ByteString
import com.Client

object Main {
  def main(args: Array[String]): Unit = {
    val host = "localhost"
    val port = 8080
    val actorSys = ActorSystem.create("MyActorSys")
    val tcpActor = actorSys.actorOf(Client.props(host, port), "client")
    Thread.sleep(1000)
    tcpActor ! ("command", "topic")
  }
}