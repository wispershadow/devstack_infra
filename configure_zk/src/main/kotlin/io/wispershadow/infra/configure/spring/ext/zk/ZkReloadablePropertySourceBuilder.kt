package io.wispershadow.infra.configure.spring.ext.zk

import io.wispershadow.infra.configure.compress.ConfigureDataCompressorType
import io.wispershadow.infra.configure.compress.ConfigureDataCompressors
import io.wispershadow.infra.configure.spring.*
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.*
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

class ZkReloadablePropertySourceBuilder(
    val configRoot: String,
    private val curatorFramework: CuratorFramework,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val compressorName: String =
            ConfigureDataCompressorType.NONE.name
) : ReloadablePropertySourceBuilder {
    companion object {
        private val logger = LoggerFactory.getLogger(ZkReloadablePropertySourceBuilder::class.java)
    }

    private var rootCache: TreeCache? = null

    private fun init(pathSegments: List<String>) {
        rootCache = createTreeCache(pathSegments)
    }

    fun destroy() {
        if (rootCache != null) {
            rootCache!!.close()
        }
    }

    fun build(pathSegments: List<String>): List<ReloadablePropertySource> {
        val result = mutableMapOf<String, ReloadablePropertySource>()
        if (rootCache == null) {
            synchronized(this) {
                init(pathSegments)
            }
        }
        populateNodes("/$configRoot", result, pathSegments)

        return result.values.toList().sortedWith(Comparator<ReloadablePropertySource> { o1, o2 ->
            o2.name.length - o1.name.length
        })
    }

    /*
     *
     */
    override fun build(applicationName: String?, activeProfiles: List<String>): List<ReloadablePropertySource> {
        val pathSegments = mutableListOf<String>()
        if (applicationName != null) {
            pathSegments.add(applicationName)
        }
        return build(pathSegments)
    }

    private fun populateNodes(curPath: String, result: MutableMap<String, ReloadablePropertySource>, pathSegments: List<String>) {
        val children = rootCache!!.getCurrentChildren(curPath)
        children?.forEach { (key, childData) ->
            try {
                val triple = convertChildData(pathSegments, childData)
                if (triple != null) {
                    result.computeIfAbsent(triple.first) {
                        ReloadablePropertySource(ReloadablePropertySourceRegistrar.PREFIX + triple.first, mapOf())
                    }.setProperty(triple.second, triple.third)
                } else {
                    logger.warn("Unable to get child data for path: {}", key)
                    populateNodes(childData.path, result, pathSegments)
                }
            } catch (e: Exception) {
                logger.error("Error converting child data, key=$key, error=${e.message}")
            }
        }
    }

    /*
     * return a pair where first element is a PropertySource key, and the second is the configuration key
     * This depends on whether the tree cache path is prefixed fully or partially with the pathSegment.
     * If so, the prefix will be part of the PropertySource key
     * this allows hierarchical configuration and tree cache ev
     */
    private fun convertKey(pathSegments: List<String>, childData: ChildData): Pair<String, String>? {
        val fullPath = childData.path
        var curPrefix = configRoot
        var curIndex = configRoot.length + 1 // include '/'
        if (curIndex < fullPath.length && fullPath[curIndex] == '/') {
            curIndex += 1
        }
        for (pathSegment in pathSegments) {
            if (curIndex < fullPath.length && curIndex + pathSegment.length <= fullPath.length) {
                val segmentToMatch = fullPath.substring(curIndex, curIndex + pathSegment.length)
                if (segmentToMatch == pathSegment) {
                    curPrefix = "$curPrefix/$pathSegment"
                    curIndex += pathSegment.length + 1
                    if (curIndex < fullPath.length && fullPath[curIndex] == '/') {
                        curIndex += 1
                    }
                } else {
                    break
                }
            } else {
                break
            }
        }
        return if (curIndex < fullPath.length) {
            val remaining = fullPath.substring(curIndex)
            Pair(curPrefix.substringAfter(configRoot),
                    remaining.split("/").filter { it.isNotBlank() }
                            .joinToString("."))
        } else {
            null
        }
    }

    private fun convertChildData(pathSegments: List<String>, childData: ChildData): Triple<String, String, Any>? {
        val key = convertKey(pathSegments, childData) ?: return null
        val value = childData.data
        if (value != null && value.isNotEmpty()) {
            val actualValue = ConfigureDataCompressors.decompress(value)
            return Triple(key.first, key.second, String(actualValue))
        }
        return null
    }

    private fun createTreeCache(pathSegments: List<String>): TreeCache {
        logger.info("Starting building tree cache for path segments: {}", pathSegments)
        val semaphore = Semaphore(0)
        val rootCache = TreeCache.newBuilder(curatorFramework, "/$configRoot")
                .setCacheData(true).setMaxDepth(20).build()
        rootCache.listenable.addListener(TreeCacheListener { client, event ->
            logger.debug("Receiving cache change event: type = {}, path = {}", event.type, event.data?.path)
            if (event.type == TreeCacheEvent.Type.NODE_ADDED ||
                    event.type == TreeCacheEvent.Type.NODE_UPDATED) {
                if (event.data != null) {
                    val changedProperty = convertChildData(pathSegments, event.data)
                    if (changedProperty != null) {
                        val properties = mapOf(changedProperty.second to changedProperty.third)
                        applicationEventPublisher.publishEvent(PropertyReloadedEvent(changedProperty.first, properties))
                    } else {
                        logger.warn("unable to convert child data for path = {}", event.data?.path)
                    }
                }
            } else if (event.type == TreeCacheEvent.Type.NODE_REMOVED) {
                val changedKey = convertKey(pathSegments, event.data)
                if (changedKey != null) {
                    applicationEventPublisher.publishEvent(PropertyRemoveEvent(changedKey.first, listOf(changedKey.second)))
                } else {
                    logger.warn("unable to convert key from path = {}", event.data?.path)
                }
            } else if (event.type == TreeCacheEvent.Type.INITIALIZED) {
                semaphore.release()
            }
        })
        rootCache.start()
        semaphore.tryAcquire(3, TimeUnit.SECONDS)
        return rootCache
    }
}