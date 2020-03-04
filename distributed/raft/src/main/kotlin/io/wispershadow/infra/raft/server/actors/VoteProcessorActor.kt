package io.wispershadow.infra.raft.server.actors

import akka.actor.ActorRef
import akka.actor.UntypedAbstractActor
import akka.event.Logging

class VoteProcessorActor(
    private val currentTerm: Long,
    private val serverId: String,
    private val logManagerActor: ActorRef,
    private val persistentManagerActor: ActorRef
) : UntypedAbstractActor() {
    private val logger = Logging.getLogger(context.system(), this)

    private class VoteTrackingInfo(
        val participants: Set<String>,
        val positiveVoter: MutableSet<String> = mutableSetOf(),
        val negativeVoter: MutableSet<String> = mutableSetOf()
    ) {
        fun onPositiveVote(serverId: String) {
            if (!participants.contains(serverId)) {
                throw IllegalArgumentException("Received positive vote from unknown server = $serverId, " +
                        "expected = $participants")
            }
            positiveVoter.add(serverId)
        }

        fun onNegativeVote(serverId: String) {
            if (!participants.contains(serverId)) {
                throw IllegalArgumentException("Received negative vote from unknown server = $serverId, " +
                        "expected = $participants")
            }
            negativeVoter.add(serverId)
        }

        fun countPositiveVoter(): Int {
            return positiveVoter.size
        }

        fun countNegativeVoter(): Int {
            return negativeVoter.size
        }

        fun isMajorityVotePositive(): Boolean {
            return positiveVoter.size > (participants.size / 2)
        }
    }

    override fun onReceive(message: Any?) {
    }
}