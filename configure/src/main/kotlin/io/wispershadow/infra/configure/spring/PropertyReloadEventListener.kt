package io.wispershadow.infra.configure.spring

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.core.env.ConfigurableEnvironment

class PropertyReloadEventListener: ApplicationContextAware {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(PropertyReloadEventListener::class.java)
    }
    private lateinit var applicationContext: ApplicationContext
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    fun reload(changedProperties: Map<String, Any>) {
        logger.info("Reloading properties: ")
        val environment = applicationContext.environment
        if (environment is ConfigurableEnvironment) {
            environment.propertySources.forEach { ps ->
                if (ps is ReloadablePropertySource) {
                    changedProperties.forEach { (key, value) ->
                        ps.setProperty(key, value)
                    }
                }
            }
        }
    }

    /**
     * After the property source has bean reloaded, we need to iterate through all beans with configurationProperties annotation or
     * values configuration
     */
    fun refreshValuesInConfiguration() {
        val configurationPropertiesBindingPostProcessor =
            applicationContext.getBean(ConfigurationPropertiesBindingPostProcessor::class.java)
        val configurationBeans = applicationContext.getBeansWithAnnotation(ConfigurationProperties::class.java)
        configurationBeans.forEach { (beanName, bean) ->
            logger.debug("Trying to refresh configuration property bean: beanName = {}, type = {}", beanName, bean::class.java.name)
            configurationPropertiesBindingPostProcessor.postProcessBeforeInitialization(bean, beanName)
        }
    }
}