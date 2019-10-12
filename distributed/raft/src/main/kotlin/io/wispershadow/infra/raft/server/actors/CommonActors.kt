package io.wispershadow.infra.raft.server.actors

import akka.actor.ActorContext
import akka.actor.ActorRef
import io.wispershadow.infra.akka.AkkaUtils
import java.time.Duration

object CommonActors {
    private const val ACTOR_PATH_LOGMANAGER = "/logManager"
    private const val ACTOR_PATH_PESISTSTATEMANAGER = "/persistStateManager"
    val ACTOR_ASK_TIMEOUT = Duration.ofMillis(2000)

}