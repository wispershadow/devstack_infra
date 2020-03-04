package io.wispershadow.infra.raft.server.actors

import akka.actor.Props
import akka.actor.UntypedAbstractActor

class PersistentStateManagerActor : UntypedAbstractActor() {
    class PersistentStateLoadRequestMessage

    companion object {
        fun props(): Props {
            return Props.create(PersistentStateManagerActor::class.java) {
                PersistentStateManagerActor()
            }
        }
    }

    override fun onReceive(message: Any?) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }
}