package io.wispershadow.infra.configure.spring

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.StandardEnvironment
import javax.annotation.PostConstruct

@Configuration
class CustomPropertiesConfig {
    @Autowired
    lateinit var environment: ConfigurableEnvironment

    //@PostConstruct
    fun addPropertySource() {
        val propertySource = getReloadablePropertySource()
        environment.propertySources?.addAfter(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, propertySource)
    }

    private fun getReloadablePropertySource(): ReloadablePropertySource {
        return ReloadablePropertySource("whatever", mutableMapOf())
    }

}