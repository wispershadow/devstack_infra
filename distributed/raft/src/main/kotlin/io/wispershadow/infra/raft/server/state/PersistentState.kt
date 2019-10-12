package io.wispershadow.infra.raft.server.state

class PersistentState {
    var currentTerm: Long = 0
    var votedFor: String? = null
}