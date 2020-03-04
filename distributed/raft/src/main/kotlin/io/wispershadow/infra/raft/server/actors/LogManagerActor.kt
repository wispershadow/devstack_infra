package io.wispershadow.infra.raft.server.actors

import akka.actor.Props
import akka.actor.UntypedAbstractActor
import io.wispershadow.infra.raft.server.log.RaftLogEntry
import java.util.*

class LogManagerActor(logFileName: String) : UntypedAbstractActor() {
    class AppendLogEntriesMessage(val entries: List<RaftLogEntry>)

    class TailLogEntryRequestMessage

    class TailLogEntryResponseMessage(val index: Long, val term: Long) {
        companion object {
            val EMPTY_RESPONSE = TailLogEntryResponseMessage(0, 0)
            fun fromLogEntry(logEntry: RaftLogEntry): TailLogEntryResponseMessage {
                return TailLogEntryResponseMessage(logEntry.index, logEntry.term)
            }
        }
    }

    class LogEntryByIndexRequestMessage(val index: Long)

    class LogEntryByIndexResponseMessage(val index: Long, val term: Long) {
        companion object {
            fun fromLogEntry(logEntry: RaftLogEntry): LogEntryByIndexResponseMessage {
                return LogEntryByIndexResponseMessage(logEntry.index, logEntry.term)
            }
        }
    }

    class LogEntryMatchRequestMessage(val index: Long, val term: Long)

    class TruncateLogRequestMessage(val index: Long, val term: Long)

    companion object {
        fun props(logFileName: String): Props {
            return Props.create(LogManagerActor::class.java) {
                LogManagerActor(logFileName)
            }
        }
    }

    override fun onReceive(message: Any?) {
        when (message) {
            is AppendLogEntriesMessage -> handleAppendLogEntries(message)
            is TailLogEntryRequestMessage -> {
                val tailLogEntryOptional = handleTailLogEntryRequest(message)
                tailLogEntryOptional.map { tailEntry ->
                    context.sender.tell(TailLogEntryResponseMessage.fromLogEntry(tailEntry), self())
                }.orElseGet {
                    context.sender.tell(TailLogEntryResponseMessage.EMPTY_RESPONSE, self())
                }
            }
            is LogEntryByIndexRequestMessage -> {
            }
            is LogEntryMatchRequestMessage -> {
            }
            is TruncateLogRequestMessage -> {
            }
        }
    }

    private fun handleAppendLogEntries(appendLogEntriesMessage: AppendLogEntriesMessage) {
    }

    private fun handleTailLogEntryRequest(tailLogEntryRequestMessage: TailLogEntryRequestMessage): Optional<RaftLogEntry> {
        return Optional.empty()
    }
}