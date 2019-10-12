package io.wispershadow.infra.configure.spring

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.StandardEnvironment

@Order(Ordered.LOWEST_PRECEDENCE)
open class CustomPropertySourceRegistrar: EnvironmentPostProcessor {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(CustomPropertySourceRegistrar::class.java)
    }

    override fun postProcessEnvironment(environment: ConfigurableEnvironment?, application: SpringApplication?) {
        logger.info("Starting post process environment, adding reloadable property source")
        val propertySource = getReloadablePropertySource(environment, application)
        environment?.propertySources
            ?.addAfter(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, propertySource)
    }

    open fun getReloadablePropertySource(environment: ConfigurableEnvironment?, application: SpringApplication?): ReloadablePropertySource {
        return ReloadablePropertySource("whatever", mutableMapOf())
    }
}