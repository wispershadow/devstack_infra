package io.wispershadow.infra.raft.server.state

open class NodeVolatileState {
    // index of highest log entry known to be committed (initialized to 0, increase monotonically)
    val commitIndex: Long = 0
    // index of highest log entry applied to state machine (initialized to 0, increase monotonically)
    val lastApplied: Long = 0
}