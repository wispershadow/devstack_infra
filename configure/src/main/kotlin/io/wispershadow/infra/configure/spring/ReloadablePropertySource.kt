package io.wispershadow.infra.configure.spring

import org.springframework.core.env.EnumerablePropertySource

class ReloadablePropertySource(key: String, properties: Map<String, Any>) :
        EnumerablePropertySource<Map<String, Any>>(key, properties) {
    private val editableProperties = mutableMapOf<String, Any>()

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
    }

    fun removeProperty(name: String) {
        editableProperties.remove(name)
    }

    fun setProperties(props: Map<String, Any>) {
        props.forEach { (key, value) ->
            editableProperties[key] = value
        }
    }

    fun removeProperties(names: Iterable<String>) {
        names.forEach { name ->
            editableProperties.remove(name)
        }
    }
}