package io.wispershadow.infra.raft.server.rpc

data class VoteRequestBuilder(
    val term: Long,
    val candidateId: String,
    var lastLogIndex: Long,
    var lastLogTerm: Long
) {
    fun lastLogIndex(lastLogIndex: Long) = apply { this.lastLogIndex = lastLogIndex }
    fun lastLogTerm(lastLogTerm: Long) = apply { this.lastLogTerm = lastLogTerm }

    fun build(): VoteRequest {
        return VoteRequest().apply {
            this.term = term
            this.candicateId = candicateId
            this.lastLogIndex = lastLogIndex
            this.lastLogTerm = lastLogTerm
        }
    }
}

data class VoteResponseBuilder(
    val requestTerm: Long,
    val responseTerm: Long,
    val voteGranted: Boolean
) {
    fun build(): VoteResponse {
        return VoteResponse().apply {
            this.requestTerm = requestTerm
            this.term = responseTerm
            this.voteGranted = voteGranted
        }
    }
}

data class AppendEntriesRequestBuilder(val term: Long)

data class AppendEntriesResponseBuilder(
    val term: Long,
    val success: Boolean,
    var nonMatchIndex: Long,
    var nonMatchTerm: Long
) {
    fun nonMatchIndex(nonMatchIndex: Long) = apply { this.nonMatchIndex = nonMatchIndex }
    fun nonMatchTerm(nonMatchTerm: Long) = apply { this.nonMatchTerm = nonMatchTerm }

    fun build(): AppendEntriesResponse {
        return AppendEntriesResponse().apply {
            this.term = term
            this.success = success
            this.nonMatchIndex = nonMatchIndex
            this.nonMatchTerm = nonMatchTerm
        }
    }
}