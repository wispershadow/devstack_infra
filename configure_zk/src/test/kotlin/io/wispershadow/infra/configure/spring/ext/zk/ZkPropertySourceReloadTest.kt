package io.wispershadow.infra.configure.spring.ext.zk

import io.mockk.mockk
import io.wispershadow.infra.configure.compress.ConfigureDataCompressorType
import io.wispershadow.infra.configure.spring.ReloadablePropertySource
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.test.TestingServer
import org.junit.AfterClass
import org.junit.Assert
import org.junit.Test
import org.springframework.context.ApplicationEventPublisher

class ZkPropertySourceReloadTest {

    companion object {
        private const val configRoot = "configroot"
        private val testingServer = TestingServer().apply {
            this.start()
        }
        private val serverPort = testingServer.port

        @AfterClass
        fun closeServer() {
            testingServer.stop()
        }
    }

    private val applicationEventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

    private fun buildCuratorFramework(): CuratorFramework {
        val zkClientBuilder = ZkClientBuilder("127.0.0.1:$serverPort", ZkClientConfig().apply {
            this.baseSleepTimeMs = 1000
            this.maxRetryTimes = 5
            this.maxSleepTimeMs = 10000
        })
        zkClientBuilder.connect()
        return zkClientBuilder.curatorFramework
    }

    @Test
    fun testInitialBuildSingleLevel() {
        val curatorFramework = buildCuratorFramework()
        try {
            loadTestData(mapOf("/$configRoot/spring/application/name" to "myApplication",
                    "/$configRoot/spring/application/profile/active" to "dev",
                    "/$configRoot/otherConfig" to "value1"), curatorFramework)
            val builder = ZkReloadablePropertySourceBuilder(configRoot, curatorFramework, applicationEventPublisher,
                    ConfigureDataCompressorType.NONE.name)
            val reloadableSources = builder.build(null, listOf("dev"))
            verifyPropSource(reloadableSources, 1, listOf("R_"), listOf(mapOf(
                    "spring.application.name" to "myApplication",
                    "spring.application.profile.active" to "dev",
                    "otherConfig" to "value1"
            )))
        } finally {
            clearData(curatorFramework)
        }
    }

    @Test
    fun testInitialBuildHierarchical() {
        val curatorFramework = buildCuratorFramework()
        try {
            loadTestData(mapOf("/$configRoot/spring/application/name" to "myApplication",
                    "/$configRoot/spring/application/profile/active" to "dev",
                    "/$configRoot/project1/subModule1/otherConfig" to "value1",
                    "/$configRoot/project1/subModule1" to "error",
                    "/$configRoot/project1/subModule1/hikari/datasource/url" to "jdbc://11212",
                    "/$configRoot/project1/hikari/datasource/url" to "jdbc://3131"), curatorFramework)
            val builder = ZkReloadablePropertySourceBuilder(configRoot, curatorFramework, applicationEventPublisher,
                    ConfigureDataCompressorType.NONE.name)
            val reloadableSources = builder.build(listOf("project1", "subModule1"))
            verifyPropSource(reloadableSources, 3, listOf("R_/project1/subModule1",
                    "R_/project1", "R_"), listOf(mapOf(
                    "otherConfig" to "value1",
                    "hikari.datasource.url" to "jdbc://11212"
            ), mapOf("hikari.datasource.url" to "jdbc://3131"),
                    mapOf("spring.application.name" to "myApplication",
                            "spring.application.profile.active" to "dev")))
        } finally {
            clearData(curatorFramework)
        }
    }

    fun clearData(curatorFramework: CuratorFramework) {
        curatorFramework.delete().deletingChildrenIfNeeded().forPath("/$configRoot")
    }

    private fun loadTestData(dataMap: Map<String, String>, curatorFramework: CuratorFramework) {
        dataMap.forEach { (key, value) ->
            ZkConfigLoadUtils.saveData(curatorFramework, key, value.toByteArray(Charsets.UTF_8))
        }
    }

    private fun verifyPropSource(
        reloadableSources: List<ReloadablePropertySource>,
        size: Int,
        propSourceNames: List<String>,
        expectedValues: List<Map<String, Any>>
    ) {
        Assert.assertEquals(reloadableSources.size, size)
        Assert.assertEquals(reloadableSources.map { it.name }, propSourceNames)
        val actualValues: List<Map<String, Any?>> = reloadableSources.map { propSource ->
            propSource.propertyNames.map { propName -> propName to propSource.getProperty(propName) }.toMap()
        }
        Assert.assertEquals(actualValues, expectedValues)
    }
}