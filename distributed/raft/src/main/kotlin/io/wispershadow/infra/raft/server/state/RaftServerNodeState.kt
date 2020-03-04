package io.wispershadow.infra.raft.server.state

class RaftServerNodeState(
    val serverId: String,
    var raftServerRole: RaftServerRole = RaftServerRole.FOLLOWER,
    var nodeVolatileState: NodeVolatileState = NodeVolatileState(),
    var nodePersistentState: PersistentState = PersistentState()
) {
    fun currentTerm(): Long {
        return nodePersistentState.currentTerm
    }
}