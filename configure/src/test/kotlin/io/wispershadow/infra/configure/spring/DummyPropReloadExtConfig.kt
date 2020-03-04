package io.wispershadow.infra.configure.spring

import org.springframework.context.annotation.Bean

@PropertyReloadExtConfig
class DummyPropReloadExtConfig {
    @Bean
    fun extBuilder(): ReloadablePropertySourceBuilder {
        return DummyReloadablePropertySourceBuilder()
    }
}

class DummyReloadablePropertySourceBuilder : ReloadablePropertySourceBuilder {
    override fun build(applicationName: String?, activeProfiles: List<String>): List<ReloadablePropertySource> {
        val propMap = mapOf("core.additional.value1" to "override", "core.property1" to "good")
        val reloadablePropertySource = ReloadablePropertySource("R_dev", propMap)
        return listOf(reloadablePropertySource)
    }
}