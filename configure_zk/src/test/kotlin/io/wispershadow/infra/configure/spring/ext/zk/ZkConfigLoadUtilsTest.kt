package io.wispershadow.infra.configure.spring.ext.zk

import org.junit.Test

class ZkConfigLoadUtilsTest {
    @Test
    fun testConfigLoadUtils() {
        ZkConfigLoadUtils.loadConfigFromYml("application-prod.yml", "", "", "")
    }
}