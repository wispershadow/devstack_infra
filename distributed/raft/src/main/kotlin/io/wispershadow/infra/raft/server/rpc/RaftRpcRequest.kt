package io.wispershadow.infra.raft.server.rpc

open class RaftRpcRequest {
    var term: Long = 0
}

open class RaftRpcResponse {
    var term: Long = 0
}