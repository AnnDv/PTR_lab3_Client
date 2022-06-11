package com

import java.net.InetSocketAddress

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.util.ByteString
import com.Client

object Main {
  def main(args: Array[String]): Unit = {
    val host = "localhost"
    val port = 8080
    // creates actor system
    val actorSys = ActorSystem.create("MyActorSys")
    // create tcp actor tells actor to start connection
    val tcpActor = actorSys.actorOf(Client.props(host, port), "client")
    // suspends the current thread
    Thread.sleep(1000)
    tcpActor ! ("connect", "md")
    Thread.sleep(1000)
    tcpActor ! ("subscribe", "de")
    Thread.sleep(1000)
    tcpActor ! ("subscribe", "en")
    Thread.sleep(3000)
    tcpActor ! ("unsubscribe", "de")
  }
}