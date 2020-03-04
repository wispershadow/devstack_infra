package io.wispershadow.infra.configure.spring

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.env.CompositePropertySource
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.PropertySource
import org.springframework.core.env.StandardEnvironment

@Configuration
@Import(ExtraConfigLoader::class)
open class PropertyReloadBootstrapConfig {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(PropertyReloadBootstrapConfig::class.java)
        private const val ATTRIB_APPLICATION_NAME = "spring.application.name"
    }

    @Bean
    @ConditionalOnClass(value = [ReloadablePropertySourceBuilder::class])
    fun reloadablePropertySource(
        environment: ConfigurableEnvironment,
        builder: ReloadablePropertySourceBuilder?
    ): PropertySource<*> {
        val applicationName = environment.getProperty(ATTRIB_APPLICATION_NAME)
        val propertySourceList = try {
            builder?.build(applicationName, environment.activeProfiles.toList()) ?: emptyList()
        } catch (e: Exception) {
            logger.error("Error reload property source: ", e)
            emptyList<ReloadablePropertySource>()
        }
        logger.info("Reloadable property sources built: {}", propertySourceList)
        val reloadablePropSource = CompositePropertySource(ReloadablePropertySourceRegistrar.RELOADABLE_PROPERTYSOURCE_KEY)
        propertySourceList.forEach { propSource ->
            reloadablePropSource.addPropertySource(propSource)
        }
        environment.propertySources.addAfter(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, reloadablePropSource)
        return reloadablePropSource
    }
}