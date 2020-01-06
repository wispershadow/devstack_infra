package io.wispershadow.infra.configure.spring.ext.zk

import io.wispershadow.infra.configure.compress.ConfigureDataCompressor
import io.wispershadow.infra.configure.compress.ConfigureDataCompressors
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.framework.state.ConnectionState
import org.apache.curator.framework.state.ConnectionStateListener
import org.apache.curator.retry.BoundedExponentialBackoffRetry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

class ZkConfigurationClient(val server: String, val root: String, val compressorName: String) {
    private val connectedFuture: CompletableFuture<Void>
    val curatorFramework: CuratorFramework
    private val defaultCompressor: ConfigureDataCompressor

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ZkConfigurationClient::class.java)
    }

    init {
        val retryPolicy = BoundedExponentialBackoffRetry(1000, 10000, 5)
        connectedFuture = CompletableFuture()
        curatorFramework = CuratorFrameworkFactory.newClient(server, retryPolicy)
        curatorFramework.connectionStateListenable.addListener(ConnectionStateListener { client: CuratorFramework?, stateChanged: ConnectionState ->
            if (stateChanged === ConnectionState.CONNECTED) {
                logger.info("Job config client connected to {}", server)
                connectedFuture.complete(null)
            }
        })
        defaultCompressor = ConfigureDataCompressors.getCompressorByName(compressorName)
    }

    fun tryConnect(): CompletableFuture<Void> {
        curatorFramework.start()
        return connectedFuture
    }

    fun connect() {
        tryConnect().get()
    }

    fun close() {
        connectedFuture.cancel(true)
        curatorFramework.close()
    }
}