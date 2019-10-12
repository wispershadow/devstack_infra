package io.wispershadow.infra.raft.server.log

class LogEntryMatchResult(val matched: Boolean, val largestTermNgRequest: Long, val lastIndexForTerm: Long) {
    companion object {
        val MATCHED = LogEntryMatchResult(true, -1, -1)
    }

}