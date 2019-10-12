package io.wispershadow.infra.raft.server.state

interface PersistentStateManager {
    fun loadPersistentState(): PersistentState

    fun savePersistentState(persistentState: PersistentState)
}