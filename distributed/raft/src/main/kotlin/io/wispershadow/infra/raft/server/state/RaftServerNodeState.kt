package io.wispershadow.infra.raft.server.state


class RaftServerNodeState(var raftServerRole: RaftServerRole,
                          val nodeVolatileState: NodeVolatileState)