package io.wispershadow.infra.configure.spring

import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [PropertyEventConfig::class, TestConfiguration1::class, TestConfiguration3::class])
@EnableConfigurationProperties
@ActiveProfiles("test")
class ConfigurablePropertyReloadTest {
    @Autowired
    lateinit var testConfiguration1: TestConfiguration1

    @Autowired
    lateinit var testConfiguration3: TestConfiguration3

    @Autowired
    lateinit var applicationEventPublisher: ApplicationEventPublisher

    companion object {
        @BeforeClass
        fun setProperties() {
            System.setProperty("spring.application.name", "applicaiton1")
        }
    }

    @Test
    fun testLoadProperties() {
        Assert.assertEquals(testConfiguration1.property1, "good")
        Assert.assertEquals(testConfiguration3.property1, "override")
    }

    @Test
    fun testReload() {
        println(testConfiguration1.configMap)
        applicationEventPublisher.publishEvent(PropertyReloadedEvent("dev",
                mapOf("core.property1" to "bad",
                        "core.nestedConfigList[0]" to "name1",
                        "core.nestedConfigList[1]" to "name2",
                        "core.configMap.key1.name" to 1)))
        Assert.assertEquals(testConfiguration1.property1, "bad")
        Assert.assertEquals(testConfiguration1.nestedConfigList.size, 2)
        Assert.assertEquals(testConfiguration1.configMap.size, 1)
    }
}