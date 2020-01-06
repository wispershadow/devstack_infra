package io.wispershadow.infra.configure.spring

interface ReloadablePropertySourceBuilder {
    fun build(): Map<String, Any>
}