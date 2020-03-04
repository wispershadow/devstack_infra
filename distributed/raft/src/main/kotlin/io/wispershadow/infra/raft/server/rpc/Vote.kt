package io.wispershadow.infra.raft.server.rpc

class VoteRequest : RaftRpcRequest() {
    lateinit var candicateId: String
    var lastLogIndex: Long = 0
    var lastLogTerm: Long = 0

    override fun toString(): String {
        return "VoteRequest(term=$term, candicateId=$candicateId, lastLogIndex=$lastLogIndex, lastLogTerm=$lastLogTerm)"
    }
}

class VoteResponse : RaftRpcResponse() {
    var voteGranted: Boolean = false
    var requestTerm: Long = 0

    override fun toString(): String {
        return "VoteResponse(term=$term, voteGranted=$voteGranted, requestTerm=$requestTerm)"
    }
}