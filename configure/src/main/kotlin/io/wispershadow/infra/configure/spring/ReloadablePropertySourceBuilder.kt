package io.wispershadow.infra.configure.spring

interface ReloadablePropertySourceBuilder {
    fun build(applicationName: String?, activeProfiles: List<String>): List<ReloadablePropertySource>
}