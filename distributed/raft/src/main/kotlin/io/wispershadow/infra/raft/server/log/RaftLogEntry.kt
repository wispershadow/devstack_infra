package io.wispershadow.infra.raft.server.log

class RaftLogEntry(val term: Long, val index: Long, val commands: ByteArray)