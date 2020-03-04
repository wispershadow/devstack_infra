package io.wispershadow.infra.raft.server.actors

import java.time.Duration

object CommonActors {
    private const val ACTOR_PATH_LOGMANAGER = "/logManager"
    private const val ACTOR_PATH_PESISTSTATEMANAGER = "/persistStateManager"
    val ACTOR_ASK_TIMEOUT = Duration.ofMillis(2000)
}