package io.wispershadow.infra.raft.server.log

import java.util.*

interface RaftLogManager {
    fun appendLogEntries(entries: List<RaftLogEntry>)

    fun getTailLogEntry(): Optional<RaftLogEntry>

    fun getLogEntryByIndex(index: Long): Optional<RaftLogEntry>

    fun logEntryMatch(index: Long, term: Long): LogEntryMatchResult

    fun truncateLog(index: Long, term: Long)
}