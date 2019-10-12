package io.wispershadow.infra.configure.spring

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [TestConfiguration1::class, TestConfiguration3::class, CustomPropertiesConfig::class])
@EnableConfigurationProperties
@ActiveProfiles("test")
class ConfigurablePropertyReloadTest {
    @Autowired
    lateinit var testConfiguration3: TestConfiguration3

    @Test
    fun testLoadProperties() {
        println(testConfiguration3.property1)
    }
}