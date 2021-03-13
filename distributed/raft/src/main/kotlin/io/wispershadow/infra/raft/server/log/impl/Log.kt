package io.wispershadow.infra.raft.server.log.impl

import java.util.concurrent.ConcurrentNavigableMap
import java.util.concurrent.ConcurrentSkipListMap

class Log {
    private val segments: ConcurrentNavigableMap<Long, LogSegment> = ConcurrentSkipListMap<Long, LogSegment>()
}