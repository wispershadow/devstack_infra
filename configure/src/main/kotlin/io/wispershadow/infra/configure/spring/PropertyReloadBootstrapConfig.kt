package io.wispershadow.infra.configure.spring

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.StandardEnvironment

@Configuration
@Import(ExtraConfigLoader::class)
open class PropertyReloadBootstrapConfig {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(PropertyReloadBootstrapConfig::class.java)
    }

    @Bean
    fun propertyReloadEventListener(): PropertyReloadEventListener {
        return PropertyReloadEventListener()
    }

    @Bean
    @ConditionalOnClass(value = [ReloadablePropertySourceBuilder::class])
    fun reloadablePropertySource(environment: ConfigurableEnvironment, builder: ReloadablePropertySourceBuilder?): ReloadablePropertySource {
        val dataMap = builder?.build() ?: emptyMap()
        logger.info("Reloadable property source built: {}", dataMap)
        val reloadablePropSource = ReloadablePropertySource(ReloadablePropertySourceRegistrar.RELOADABLE_PROPERTYSOURCE_KEY,
                dataMap)
        environment.propertySources.addAfter(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, reloadablePropSource)
        return reloadablePropSource
    }
}