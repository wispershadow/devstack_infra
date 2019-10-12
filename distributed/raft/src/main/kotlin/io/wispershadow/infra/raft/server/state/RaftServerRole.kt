package io.wispershadow.infra.raft.server.state

enum class RaftServerRole {
    FOLLOWER,
    CANDIDATE,
    LEADER
}