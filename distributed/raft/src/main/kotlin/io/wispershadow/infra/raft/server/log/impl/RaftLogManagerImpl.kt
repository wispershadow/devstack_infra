package io.wispershadow.infra.raft.server.log.impl

import io.wispershadow.infra.raft.server.log.LogEntryMatchResult
import io.wispershadow.infra.raft.server.log.RaftLogEntry
import io.wispershadow.infra.raft.server.log.RaftLogManager
import java.util.*

class RaftLogManagerImpl : RaftLogManager {

    override fun appendLogEntries(entries: List<RaftLogEntry>) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun getTailLogEntry(): Optional<RaftLogEntry> {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun getLogEntryByIndex(index: Long): Optional<RaftLogEntry> {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun logEntryMatch(index: Long, term: Long): LogEntryMatchResult {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun truncateLog(index: Long, term: Long) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }
}