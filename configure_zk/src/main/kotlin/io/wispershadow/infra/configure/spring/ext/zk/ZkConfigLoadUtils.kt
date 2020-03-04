package io.wispershadow.infra.configure.spring.ext.zk

import io.wispershadow.infra.configure.compress.ConfigureDataCompressorType
import io.wispershadow.infra.configure.compress.ConfigureDataCompressors
import org.apache.curator.framework.CuratorFramework
import org.apache.zookeeper.KeeperException
import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.core.env.EnumerablePropertySource
import org.springframework.core.io.ClassPathResource

object ZkConfigLoadUtils {
    fun loadConfigFromYml(
        ymlConfigFileName: String,
        zkAddress: String,
        configRoot: String,
        activeProfile: String
    ) {
        val classPathResource = ClassPathResource(ymlConfigFileName)
        val propSourceList = YamlPropertySourceLoader().load("data", classPathResource)
        propSourceList.forEach { propSource ->
            if (propSource is EnumerablePropertySource) {
                propSource.propertyNames.forEach { propName ->
                    println(propName)
                }
            }
        }
        /*
        val zkClientBuilder = ZkClientBuilder(zkAddress, ZkClientConfig())
        zkClientBuilder.connect()
        val curatorFramework = zkClientBuilder.curatorFramework
        val fullPath = ZKPaths.makePath("${configRoot}/$activeProfile", "/spring")
        try {
            val data = "".toByteArray(Charsets.UTF_8)

        } catch (ex: Exception) {
            throw RuntimeException(ex.message, ex)
        }
         */
    }

    fun saveData(
        curatorFramework: CuratorFramework,
        path: String,
        data: ByteArray,
        compressorName: String = ConfigureDataCompressorType.NONE.name
    ) {
        val actualData = ConfigureDataCompressors.compress(ConfigureDataCompressors.getCompressorByName(compressorName), data)
        var exists = curatorFramework.checkExists().forPath(path) != null
        while (true) {
            exists = try {
                if (exists) {
                    curatorFramework.setData().forPath(path, actualData)
                } else {
                    curatorFramework.create().creatingParentsIfNeeded().forPath(path, actualData)
                }
                return
            } catch (e: KeeperException.NodeExistsException) {
                true
            } catch (e: KeeperException.NoNodeException) {
                false
            }
        }
    }
}