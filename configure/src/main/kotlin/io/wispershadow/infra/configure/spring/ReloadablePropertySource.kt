package io.wispershadow.infra.configure.spring

import org.springframework.context.ApplicationEventPublisher
import org.springframework.core.env.EnumerablePropertySource

class ReloadablePropertySource(key: String, properties: Map<String, Any>) :
        EnumerablePropertySource<Map<String, Any>>(key, properties) {
    private val editableProperties = mutableMapOf<String, Any>()
    var reloadPropertyEventPublisher: ApplicationEventPublisher? = null

    init {
        editableProperties.putAll(properties)
    }

    override fun getPropertyNames(): Array<String> {
        return editableProperties.keys.toTypedArray()
    }

    override fun getProperty(name: String): Any? {
        return editableProperties[name]
    }

    fun setProperty(name: String, value: Any) {
        editableProperties[name] = value
        reloadPropertyEventPublisher?.publishEvent(PropertyReloadedEvent(mapOf(name to value)))
    }
}