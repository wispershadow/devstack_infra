package io.wispershadow.infra.configure.spring

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.Banner
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.ParentContextApplicationContextInitializer
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.boot.env.OriginTrackedMapPropertySource
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.env.*
import org.springframework.core.env.PropertySource.StubPropertySource
import org.springframework.util.ReflectionUtils
import java.util.*

@Order(Ordered.LOWEST_PRECEDENCE)
class ReloadablePropertySourceRegistrar : EnvironmentPostProcessor {
    companion object {
        val RELOADABLE_PROPERTYSOURCE_KEY = "RELOADABLE"
        val PREFIX = "R_"
        private val DEFAULT_PROPERTIES = "defaultProperties"
        private val BOOTSTRAP_PROPERTY_SOURCE_NAME = "bootstrap"
        private val logger: Logger = LoggerFactory.getLogger(ReloadablePropertySourceRegistrar::class.java)
    }

    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication) {
        if (environment.propertySources.contains(BOOTSTRAP_PROPERTY_SOURCE_NAME)) {
            return
        }
        if (environment.propertySources.contains(RELOADABLE_PROPERTYSOURCE_KEY)) {
            return
        }
        var context: ConfigurableApplicationContext? = null
        try {
            for (initializer in application.initializers) {
                if (initializer is ParentContextApplicationContextInitializer) {
                    context = findBootstrapContext(initializer)
                }
            }
            if (context == null) {
                context = bootstrapServiceContext(environment, application)
            }
        } catch (e: Exception) {
            logger.error("Error processing reloadable property source", e)
        }
    }

    private fun findBootstrapContext(initializer: ParentContextApplicationContextInitializer): ConfigurableApplicationContext? {
        val field = ReflectionUtils
                .findField(ParentContextApplicationContextInitializer::class.java, "parent")
        ReflectionUtils.makeAccessible(field)
        var parent: ConfigurableApplicationContext? = safeCast(
                ConfigurableApplicationContext::class.java,
                ReflectionUtils.getField(field, initializer))
        if (parent != null) {
            parent = safeCast(ConfigurableApplicationContext::class.java, parent.parent)
        }
        return parent
    }

    private fun <T> safeCast(type: Class<T>, value: Any?): T? {
        return try {
            type.cast(value)
        } catch (e: ClassCastException) {
            null
        }
    }

    private fun bootstrapServiceContext(environment: ConfigurableEnvironment, application: SpringApplication):
            ConfigurableApplicationContext {
        val bootstrapEnvironment = StandardEnvironment()
        val bootstrapProperties = bootstrapEnvironment.propertySources
        for (source in bootstrapProperties) {
            bootstrapProperties.remove(source.name)
        }
        val bootstrapMap = mutableMapOf<String, Any>()
        bootstrapProperties.addFirst(
                MapPropertySource(BOOTSTRAP_PROPERTY_SOURCE_NAME, bootstrapMap))

        // TODO: is it possible or sensible to share a ResourceLoader?
        for (source in environment.propertySources) {
            if (source is StubPropertySource) {
                continue
            }
            bootstrapProperties.addLast(source)
        }
        val builder = SpringApplicationBuilder()
                .profiles(*environment.activeProfiles).bannerMode(Banner.Mode.OFF)
                .environment(bootstrapEnvironment) // Don't use the default properties in this builder
                .registerShutdownHook(false).logStartupInfo(false)
                .web(WebApplicationType.NONE)
        val builderApplication = builder.application()
        if (builderApplication.mainApplicationClass == null) {
            // gh_425:
            // SpringApplication cannot deduce the MainApplicationClass here
            // if it is booted from SpringBootServletInitializer due to the
            // absense of the "main" method in stackTraces.
            // But luckily this method's second parameter "application" here
            // carries the real MainApplicationClass which has been explicitly
            // set by SpringBootServletInitializer itself already.
            builder.main(application.mainApplicationClass)
        }
        builder.sources(PropertyReloadBootstrapConfig::class.java)
        return builder.run().apply {
            this.id = "bootstrap"
            addAncestorInitializer(application, this)
            // It only has properties in it now that we don't want in the parent so remove
            // it  (and it will be added back later)
            bootstrapProperties.remove(BOOTSTRAP_PROPERTY_SOURCE_NAME)
            mergeDefaultProperties(environment.propertySources, bootstrapProperties)
        }
    }

    private fun addAncestorInitializer(
        application: SpringApplication,
        context: ConfigurableApplicationContext
    ) {
        var installed = false
        for (initializer in application
                .initializers) {
            if (initializer is AncestorInitializer) {
                installed = true
                // New parent
                initializer.parent = context
            }
        }
        if (!installed) {
            application.addInitializers(AncestorInitializer(context))
        }
    }

    private fun mergeDefaultProperties(
        environment: MutablePropertySources,
        bootstrap: MutablePropertySources
    ) {
        val name: String = DEFAULT_PROPERTIES
        if (bootstrap.contains(name)) {
            val source = bootstrap[name]
            if (!environment.contains(name)) {
                environment.addLast(source)
            } else {
                val target = environment[name]
                if (target is MapPropertySource && target !== source && source is MapPropertySource) {
                    val targetMap = target.source
                    val map = source.source
                    for (key in map.keys) {
                        if (!target.containsProperty(key)) {
                            targetMap[key] = map[key]
                        }
                    }
                }
            }
        }
        mergeAdditionalPropertySources(environment, bootstrap)
    }

    private fun mergeAdditionalPropertySources(
        environment: MutablePropertySources,
        bootstrap: MutablePropertySources
    ) {
        for (source in bootstrap) {
            addOrReplace(environment, source)
        }
    }

    private fun addOrReplace(
        environment: MutablePropertySources,
        result: PropertySource<*>
    ) {
        if (environment.contains(result.name)) {
            environment.replace(result.name, result)
        } else {
            environment.addAfter(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, result)
        }
    }

    class AncestorInitializer(var parent: ConfigurableApplicationContext) : ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {
        override fun initialize(context: ConfigurableApplicationContext) {
            var currentContext: ConfigurableApplicationContext = context
            while (currentContext.parent != null && currentContext.parent !== currentContext) {
                currentContext = currentContext.parent as ConfigurableApplicationContext
            }
            reorderSources(currentContext.environment)
            ParentContextApplicationContextInitializer(parent)
                    .initialize(currentContext)
        }

        private fun reorderSources(environment: ConfigurableEnvironment) {
            val removed = environment.propertySources
                    .remove(DEFAULT_PROPERTIES)
            if (removed is ExtendedDefaultPropertySource) {
                environment.propertySources.addLast(MapPropertySource(DEFAULT_PROPERTIES, removed.source))
                for (source in removed.getPropertySources().propertySources) {
                    if (!environment.propertySources.contains(source.name)) {
                        environment.propertySources.addBefore(DEFAULT_PROPERTIES, source)
                    }
                }
            }
        }

        override fun getOrder(): Int {
            return Ordered.HIGHEST_PRECEDENCE + 5
        }
    }

    class ExtendedDefaultPropertySource(name: String, propertySource: PropertySource<*>) : SystemEnvironmentPropertySource(name, findMap(propertySource)) {
        val names = mutableListOf<String>()
        val sources: CompositePropertySource = CompositePropertySource(name)

        companion object {
            fun findMap(propertySource: PropertySource<*>): Map<String, Any?>? {
                return if (propertySource is MapPropertySource) {
                    propertySource.source as Map<String, Any?>
                } else LinkedHashMap()
            }
        }

        fun add(source: PropertySource<*>) { // Only add map property sources added by boot, see gh-476
            if (source is OriginTrackedMapPropertySource &&
                    !names.contains(source.getName())) {
                sources.addPropertySource(source)
                names.add(source.getName())
            }
        }

        fun getPropertySources(): CompositePropertySource {
            return sources
        }

        fun getPropertySourceNames(): List<String?> {
            return names
        }

        override fun getProperty(name: String?): Any? {
            return if (sources.containsProperty(name)) {
                sources.getProperty(name)
            } else super.getProperty(name)
        }

        override fun containsProperty(name: String?): Boolean {
            return if (sources.containsProperty(name)) {
                true
            } else super.containsProperty(name)
        }

        override fun getPropertyNames(): Array<String>? {
            val names: MutableList<String> = ArrayList()
            names.addAll(listOf(*sources.propertyNames))
            names.addAll(listOf(*super.getPropertyNames()))
            return names.toTypedArray()
        }
    }
}