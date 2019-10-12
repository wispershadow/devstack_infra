package io.wispershadow.infra.raft.server

import io.wispershadow.infra.raft.server.log.RaftLogManager
import io.wispershadow.infra.raft.server.rpc.VoteRequest
import io.wispershadow.infra.raft.server.rpc.VoteResponse
import io.wispershadow.infra.raft.server.rpc.VoteResponseBuilder
import io.wispershadow.infra.raft.server.state.PersistentState
import io.wispershadow.infra.raft.server.state.RaftServerNodeState
import org.slf4j.LoggerFactory

class VoteProcessor {
    private val logger = LoggerFactory.getLogger(VoteProcessor::class.java)



    fun processVoteRequest(voteRequest: VoteRequest,
                           serverNodeState: RaftServerNodeState,
                           loadPersistentStateFun: () -> PersistentState): VoteProcessResult {
        logger.info("Start processing vote request = {}", voteRequest)
        val persistentState = loadPersistentStateFun.invoke()
        val currentTerm = persistentState.currentTerm
        val leaderId = persistentState.votedFor
        val requestTerm = voteRequest.term
        if (requestTerm < currentTerm) {
            logger.warn("Vote request is of lower term, receivedTerm = {}, currentTerm = {}",
                requestTerm, currentTerm)
            return VoteProcessResult(RpcProcessResultCommand.DISCARD,
                VoteResponseBuilder(currentTerm, false).build())
        }
        else if (requestTerm > currentTerm) {
            logger.warn("Vote request is of higher term, receivedTerm = {}, currentTerm ={}",
                requestTerm, currentTerm)
        }
    }

    fun makeVoteDecision(voteRequest: VoteRequest, leaderId: String?,
                         logManager: RaftLogManager): VoteResponse {
        if (leaderId != null && leaderId != voteRequest.candicateId) {
            logger.warn("Mismatch leader, candidateId = {}, currentVoteFor = {}", )
        }
        logManager.getTailLogEntry().map {tailEntry ->

        }
    }
}

class VoteProcessResult(val processResultCommand: RpcProcessResultCommand,
                        val voteResponse: VoteResponse,
                        var leaderId: String? = null)

enum class RpcProcessResultCommand {
    DISCARD,
    UPGRADE
}
