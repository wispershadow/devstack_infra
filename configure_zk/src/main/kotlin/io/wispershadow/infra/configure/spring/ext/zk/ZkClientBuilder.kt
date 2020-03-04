package io.wispershadow.infra.configure.spring.ext.zk

import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.framework.state.ConnectionState
import org.apache.curator.framework.state.ConnectionStateListener
import org.apache.curator.retry.BoundedExponentialBackoffRetry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

class ZkClientBuilder(val server: String, zkClientConfig: ZkClientConfig) {
    private val connectedFuture: CompletableFuture<Void>
    val curatorFramework: CuratorFramework

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ZkClientBuilder::class.java)
    }

    init {
        val retryPolicy = BoundedExponentialBackoffRetry(zkClientConfig.baseSleepTimeMs, zkClientConfig.maxSleepTimeMs, zkClientConfig.maxRetryTimes)
        connectedFuture = CompletableFuture()
        curatorFramework = CuratorFrameworkFactory.newClient(server, retryPolicy)
        curatorFramework.connectionStateListenable.addListener(ConnectionStateListener { _: CuratorFramework?, stateChanged: ConnectionState ->
            if (stateChanged === ConnectionState.CONNECTED) {
                logger.info("Job config client connected to {}", server)
                connectedFuture.complete(null)
            }
        })
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