package io.wispershadow.infra.configure.spring

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "core")
class TestConfiguration1 {
    var property1: String? = null
    var property2: String? = null
}