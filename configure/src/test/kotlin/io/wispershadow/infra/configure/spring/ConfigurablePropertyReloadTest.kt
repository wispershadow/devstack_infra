package io.wispershadow.infra.configure.spring

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [TestConfiguration1::class, TestConfiguration3::class])
@EnableConfigurationProperties
@ActiveProfiles("test")
class ConfigurablePropertyReloadTest {
    @Autowired
    lateinit var testConfiguration1: TestConfiguration1

    @Autowired
    lateinit var testConfiguration3: TestConfiguration3

    @Test
    fun testLoadProperties() {
        Assert.assertEquals(testConfiguration1.property1, "good")
        Assert.assertEquals(testConfiguration3.property1, "override")
    }
}