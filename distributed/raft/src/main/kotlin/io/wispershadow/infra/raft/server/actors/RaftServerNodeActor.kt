package io.wispershadow.infra.raft.server.actors

import akka.actor.AbstractActor
import akka.actor.ActorRef
import akka.actor.Cancellable
import akka.event.Logging

class RaftServerNodeActor: AbstractActor() {
    private val log = Logging.getLogger(context.system(), this)
    private val peerNodeActorRefs = mutableMapOf<String, ActorRef>()
    private val followerReceive: Receive = buildFollowerReceive()
    private val candidateReceive: Receive = buildCandidateReceive()
    private val leaderReceive: Receive = buildLeaderReceive()
    private val schedulers = mutableMapOf<String, Cancellable>()



    private fun buildFollowerReceive(): Receive {
        return receiveBuilder().build()
    }

    private fun buildCandidateReceive(): Receive {
        return receiveBuilder().build()
    }

    private fun buildLeaderReceive(): Receive {
        return receiveBuilder().build()
    }


    override fun createReceive(): Receive {
        return receiveBuilder().build()
    }
}