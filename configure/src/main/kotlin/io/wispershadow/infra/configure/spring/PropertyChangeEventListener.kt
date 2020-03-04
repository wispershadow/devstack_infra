package io.wispershadow.infra.configure.spring

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.support.AbstractBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor
import org.springframework.boot.context.properties.source.ConfigurationPropertyName
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationListener
import org.springframework.core.env.CompositePropertySource
import org.springframework.core.env.ConfigurableEnvironment

class PropertyChangeEventListener : ApplicationListener<PropertyChangeEvent>, ApplicationContextAware {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(PropertyChangeEventListener::class.java)
    }

    private lateinit var applicationContext: ApplicationContext
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    override fun onApplicationEvent(event: PropertyChangeEvent) {
        val env = applicationContext.environment
        if (env is ConfigurableEnvironment) {
            env.propertySources.filter { propertySource ->
                propertySource is CompositePropertySource && propertySource.name ==
                        ReloadablePropertySourceRegistrar.RELOADABLE_PROPERTYSOURCE_KEY
            }.forEach {
                val compositePropertySource = it as CompositePropertySource
                val reloadablePropertySource = compositePropertySource.propertySources.find { reloadablePs ->
                    reloadablePs.name == ReloadablePropertySourceRegistrar.PREFIX + event.propertySourceKey
                } as ReloadablePropertySource?
                if (reloadablePropertySource != null) {
                    logger.info("Setting properties for reloadable property sources")
                    if (event is PropertyReloadedEvent) {
                        reloadablePropertySource.setProperties(
                                event.changedProperties.mapKeys { propEntry -> convertPropName(propEntry.key)
                                })
                    } else if (event is PropertyRemoveEvent) {
                        reloadablePropertySource.removeProperties(
                                event.removedPropertyNames.map { propName -> convertPropName(propName)
                                })
                    }
                }
            }
        }
        val configurationPropertiesBindingPostProcessor = getConfigPropBindPostProcessor()
        if (configurationPropertiesBindingPostProcessor != null) {
            val configurationBeans = applicationContext.getBeansWithAnnotation(ConfigurationProperties::class.java)
            configurationBeans.forEach { (beanName, bean) ->
                logger.debug("Trying to refresh configuration property bean: beanName = {}, type = {}", beanName, bean::class.java.name)
                try {
                    configurationPropertiesBindingPostProcessor.postProcessBeforeInitialization(bean, beanName)
                } catch (e: Exception) {
                    // in case of misconfiguration
                    logger.error("Error refresh properties for bean, name=$beanName", e)
                }
            }
        }
        val autowiredAnnotationBeanPostProcessor = getAutowirePostProcessor()
        if (applicationContext is BeanDefinitionRegistry) {
            val beanDefRegistry = applicationContext as BeanDefinitionRegistry
            if (autowiredAnnotationBeanPostProcessor != null) {
                val valueBeans = applicationContext.getBeansWithAnnotation(Value::class.java)
                valueBeans.forEach { (beanName, bean) ->
                    logger.debug("Trying to refresh value bean: beanName = {}, type ={}", beanName, bean::class.java.name)
                    val beanDefinition = beanDefRegistry.getBeanDefinition(beanName)
                    // autowiredAnnotationBeanPostProcessor.postProcessMergedBeanDefinition()
                }
            }
        }
    }

    private fun getConfigPropBindPostProcessor(): ConfigurationPropertiesBindingPostProcessor? {
        val beanFactory = applicationContext.autowireCapableBeanFactory
        if (beanFactory is AbstractBeanFactory) {
            return beanFactory.beanPostProcessors.find {
                it is ConfigurationPropertiesBindingPostProcessor
            } as ConfigurationPropertiesBindingPostProcessor?
        }
        return null
    }

    private fun getAutowirePostProcessor(): AutowiredAnnotationBeanPostProcessor? {
        val beanFactory = applicationContext.autowireCapableBeanFactory
        if (beanFactory is AbstractBeanFactory) {
            return beanFactory.beanPostProcessors.find {
                it is AutowiredAnnotationBeanPostProcessor
            } as AutowiredAnnotationBeanPostProcessor?
        }
        return null
    }

    private fun convertPropName(propName: String): String {
        return try {
            ConfigurationPropertyName.of(propName).toString()
        } catch (e: Exception) {
            propName
        }
    }
}