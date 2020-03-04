package io.wispershadow.infra.configure.spring

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PropertyEventConfig {
    @Bean
    fun propertyReloadEventListener(): PropertyChangeEventListener {
        return PropertyChangeEventListener()
    }
}