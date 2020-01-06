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
    override fun build(): Map<String, Any> {
        return mapOf("core.additional.value1" to "override", "core.property1" to "good")
    }
}