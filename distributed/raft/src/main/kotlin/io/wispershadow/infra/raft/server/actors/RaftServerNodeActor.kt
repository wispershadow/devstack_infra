package io.wispershadow.infra.raft.server.actors

import akka.actor.AbstractActor
import akka.actor.ActorRef
import akka.actor.Cancellable
import akka.event.Logging
import io.wispershadow.infra.raft.server.rpc.RaftRpcRequest
import io.wispershadow.infra.raft.server.rpc.RaftRpcResponse
import io.wispershadow.infra.raft.server.state.RaftServerNodeState
import io.wispershadow.infra.raft.server.state.RaftServerRole
import java.lang.Exception
import java.time.Duration
import java.util.*
import kotlin.random.Random

class RaftServerNodeActor : AbstractActor() {
    private val log = Logging.getLogger(context.system(), this)
    private val peerNodeActorRefs = mutableMapOf<String, ActorRef>()

    private val schedulersByTerm: NavigableMap<Long, MutableList<Cancellable>> =
            TreeMap<Long, MutableList<Cancellable>>()
    private var currentServerNodeState: RaftServerNodeState = RaftServerNodeState(generateServerId())

    class ElectionTimeoutMessage()

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(RaftRpcRequest::class.java) {
                }
                .match(RaftRpcResponse::class.java) {
                }
                .match(ElectionTimeoutMessage::class.java) {
                }
                .matchAny {
                }
                .build()
    }

    private fun bootstrap() {
        // todo: load persist state, discover peer
    }

    private fun generateServerId(): String {
        return UUID.randomUUID().toString()
    }

    /*
    Raft uses randomized election timeouts to ensure that split votes are rare and that they are resolved quickly.
    To prevent split votes in the first place, election timeouts are chosen randomly from a fixed interval
    (e.g., 150–300ms)
     */
    private fun getNextElectionTimeoutMs(): Long {
        return Random.Default.nextLong(150, 300)
    }

    private fun becomeFollower() {
        val currentTerm = currentServerNodeState.currentTerm()
        log.info("Raft server: {} becomes follower for term: {}", currentServerNodeState.serverId,
                currentTerm)
        currentServerNodeState.raftServerRole = RaftServerRole.FOLLOWER
        val nextElectionTimeout = getNextElectionTimeoutMs()
        /*
         If a follower receives no communication over a period of time called the election timeout,
         then it assumes there is no viable leader and begins an election to choose a new leader
         */
        val electionTimeoutFuture = context.system.scheduler.scheduleOnce(Duration.ofMillis(nextElectionTimeout), self, ElectionTimeoutMessage(),
                context.system.dispatcher(), ActorRef.noSender())
        schedulersByTerm.computeIfAbsent(currentTerm) {
            mutableListOf()
        }.add(electionTimeoutFuture)
    }

    private fun cleanUpPrevTermSchedulers(curTerm: Long) {
        val headerMap = schedulersByTerm.headMap(curTerm, false)
        headerMap.forEach { (term, schedules) ->
            schedules.forEach {
                try {
                    it.cancel()
                } catch (e: Exception) {
                    log.error("Error cancelling scheduler for term = {}, ")
                }
            }
        }
        headerMap.clear()
    }

    private fun handleRaftRcpRequest(raftRpcRequest: RaftRpcRequest) {
        val requestTerm = raftRpcRequest.term
        val currentTerm = currentServerNodeState.currentTerm()
        if (requestTerm > currentTerm) {

            cleanUpPrevTermSchedulers(requestTerm)
            // if any error occurs before this step
        }

    }

    private fun handleRaftRpcResponse(raftRpcResponse: RaftRpcResponse) {

    }


    private fun newElectionOnTimeout() {
        val curTerm = currentServerNodeState.currentTerm()
        val newTerm = updateTerm(curTerm,
                currentServerNodeState.serverId, RaftServerRole.CANDIDATE)
        log.info("Raft server: {} becomes candidate for term: {}", currentServerNodeState.serverId,
                newTerm)
        currentServerNodeState.raftServerRole = RaftServerRole.CANDIDATE
        cleanUpPrevTermSchedulers(curTerm)
    }

    /*
     Set term and must call persistStateManager to persist it
     New role will be either Candidate or Follower depending on the increasing term is started by election timeout or
     RPC request/response with higher term
     */
    private fun updateTerm(curTerm: Long, serverId: String, newRole: RaftServerRole): Long {
        return 0
    }
}