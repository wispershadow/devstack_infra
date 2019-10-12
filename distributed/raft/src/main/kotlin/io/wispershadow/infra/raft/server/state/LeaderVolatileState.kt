package io.wispershadow.infra.raft.server.state

class LeaderVolatileState: NodeVolatileState() {
    // for each server, index of the next log entry to send to that server (initialized to leader's last log index + 1)
    val nextIndices = mutableListOf<Long>()
    val matchIndices = mutableListOf<Long>()
}