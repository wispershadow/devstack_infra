package io.wispershadow.infra.raft.server.actors

import akka.actor.UntypedAbstractActor
import akka.event.Logging

class AppendEntriesProcessorActor(
    private val currentTerm: Long,
    private val serverId: String
) : UntypedAbstractActor() {
    private val logger = Logging.getLogger(context.system(), this)

    override fun onReceive(message: Any?) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }
}