package io.wispershadow.infra.raft.server.actors

import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.UntypedAbstractActor
import akka.event.Logging
import akka.pattern.Patterns
import io.wispershadow.infra.raft.server.rpc.VoteRequest
import io.wispershadow.infra.raft.server.rpc.VoteResponse
import io.wispershadow.infra.raft.server.rpc.VoteResponseBuilder
import io.wispershadow.infra.raft.server.state.PersistentState
import io.wispershadow.infra.raft.server.state.RaftServerRole
import java.util.concurrent.CompletionStage

class VoteProcessorActor(
    private val currentTerm: Long,
    private val serverId: String,
    private val logManagerActor: ActorRef,
    private val persistentManagerActor: ActorRef
): UntypedAbstractActor() {
    private val logger = Logging.getLogger(context.system(), this)

    class VoteRequestMessage(val serverRole: RaftServerRole, val voteRequest: VoteRequest)
    class VoteResponseMessage(val serverId: String, val voteResponse: VoteResponse)
    class LaunchVoteRequestMessage(val term: Long, val participants: Set<String>)
    class CancelVoteRequestMessage(val term: Long)

    private class VoteTrackingInfo(val participants: Set<String>,
                                   val positiveVoter: MutableSet<String> = mutableSetOf(),
                                   val negativeVoter: MutableSet<String> = mutableSetOf()) {
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

    companion object {
        fun props(currentTerm: Long, serverId: String, logManagerActor: ActorRef, persistentManagerActor: ActorRef): Props {
            return Props.create(VoteProcessorActor::class.java) {
                VoteProcessorActor(currentTerm, serverId, logManagerActor, persistentManagerActor)
            }
        }
    }

    //
    private val voteCountMap = mutableMapOf<Long, VoteTrackingInfo>()


    override fun onReceive(message: Any?) {
        when (message) {
            is VoteRequestMessage -> processVoteRequest(message)
            is LaunchVoteRequestMessage -> launchVote(message)
        }
    }

    private fun launchVote(launchVoteRequestMessage: LaunchVoteRequestMessage) {
        val launchVoteTerm = launchVoteRequestMessage.term
        if (launchVoteTerm != currentTerm) {
            logger.error("Mismatch term id on launch vote request, launchVoteTerm = {}, " +
                    "currentTerm = {}", launchVoteTerm, currentTerm)
        }
        if (voteCountMap.containsKey(currentTerm)) {
            logger.error("Vote $currentTerm is already launched")
            return
        }
        val allServerIds = launchVoteRequestMessage.participants
        voteCountMap[currentTerm] = VoteTrackingInfo(allServerIds.minus(serverId))

    }

    private fun cancelVote() {

    }

    private fun processVoteRequest(voteRequestMessage: VoteRequestMessage) {
        val persistentStateFuture = Patterns.ask(persistentManagerActor,
            PersistentStateManagerActor.PersistentStateLoadRequestMessage(), CommonActors.ACTOR_ASK_TIMEOUT)
        as CompletionStage<PersistentState>
        val voteRequest = voteRequestMessage.voteRequest
        persistentStateFuture.handle{persistentState, throwable ->
            if (persistentState != null) {
                logger.debug("Getting persistence state, term = {}, leaderId = {}",
                    persistentState.currentTerm, persistentState.votedFor)
                val currentTerm = persistentState.currentTerm
                val leaderId = persistentState.votedFor
                val requestTerm = voteRequest.term
                when {
                    (requestTerm < currentTerm) -> {
                        logger.info(
                            "Vote request is of lower term, receivedTerm = {}, currentTerm = {}",
                            requestTerm, currentTerm
                        )
                        val response = VoteResponseBuilder(currentTerm, false).build()
                        context.sender.tell(response, self)
                    }
                    (requestTerm > currentTerm) -> {
                        logger.info(
                            "Vote request is of higher term, receivedTerm = {}, currentTerm ={}",
                            requestTerm, currentTerm
                        )
                        //TODO: raise term
                    }
                    else -> makeVoteDecision(voteRequest, currentTerm, leaderId)
                }
            }
            else if (throwable != null) {
                logger.error("Error getting persistent state", throwable)
            }
        }
    }

    private fun processVoteResponse(voteResponseMessage: VoteResponseMessage) {
        val responseServerId = voteResponseMessage.serverId
        val responseTerm = voteResponseMessage.voteResponse.term
        if (currentTerm != responseTerm) {
            logger.error("Mismatch vote response term, voteResponseTerm = {}, currentTerm = {}", responseTerm, currentTerm)
            return
        }
        val voteGranted = voteResponseMessage.voteResponse.voteGranted
        val voteTrackingInfo = voteCountMap.getValue(responseTerm)
        if (voteGranted) {
            if (voteTrackingInfo.positiveVoter.contains(responseServerId)) {
                logger.info("Received duplicated positive vote response from server: {}", responseServerId)
            }
            voteTrackingInfo.onPositiveVote(responseServerId)
            if (voteTrackingInfo.isMajorityVotePositive()) {
                //TODO: become leader
            }
        }
        else {
            if (voteTrackingInfo.negativeVoter.contains(responseServerId)) {
                logger.info("Received duplicated negative vote response from server: {}", responseServerId)
            }
            voteTrackingInfo.onNegativeVote(responseServerId)
        }
    }


    private fun makeVoteDecision(voteRequest: VoteRequest, currentTerm: Long, leaderId: String?) {
        if (leaderId != null && leaderId != voteRequest.candicateId) {
            logger.info("Mismatch leader, candidateId = {}, currentVoteFor = {}", leaderId)
        }
        val tailLogFuture = Patterns.ask(logManagerActor, LogManagerActor.TailLogEntryRequestMessage(),
            CommonActors.ACTOR_ASK_TIMEOUT) as CompletionStage<LogManagerActor.TailLogEntryResponseMessage>
        tailLogFuture.handle {tailLogResponse, throwable ->
            logger.debug("Getting tail log, term = {}, index = {}", tailLogResponse.term,
                tailLogResponse.index)
            if (tailLogResponse != null) {
                val voteYes = when {
                    tailLogResponse == LogManagerActor.TailLogEntryResponseMessage.EMPTY_RESPONSE -> true
                    isVoteRequestStale(tailLogResponse, voteRequest) -> false
                    else -> true
                }
                val response = VoteResponseBuilder(currentTerm, voteYes).build()
                context.sender.tell(response, self)
            }
            else if (throwable != null) {
                logger.error("Error making vote decision", throwable)
            }
        }
    }

    private fun isVoteRequestStale(tailLogEntryResponseMessage: LogManagerActor.TailLogEntryResponseMessage,
                           voteRequest: VoteRequest): Boolean {
        return (tailLogEntryResponseMessage.term > voteRequest.lastLogTerm) ||
                (tailLogEntryResponseMessage.term == voteRequest.term &&
                        tailLogEntryResponseMessage.index > voteRequest.lastLogIndex)
    }



}