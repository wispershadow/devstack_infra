package io.wispershadow.infra.raft.server.rpc

import io.wispershadow.infra.raft.server.log.RaftLogEntry

class AppendEntriesRequest: RaftRpcRequest() {
    lateinit var leaderId: String
    var prevLogIndex: Long = 0
    var prevLogTerm: Long = 0
    var entries: List<RaftLogEntry> = emptyList()
    var leaderCommit: Long = 0
}

class AppendEntriesResponse: RaftRpcResponse() {
    var success: Boolean = true
    var nonMatchIndex: Long = 0
    var nonMatchTerm: Long = 0
}