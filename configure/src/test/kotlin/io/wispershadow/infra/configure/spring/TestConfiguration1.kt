package io.wispershadow.infra.configure.spring

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "core")
class TestConfiguration1 {
    var property1: String? = null
    var property2: String? = null
    var nestedConfigList: List<String> = emptyList()
    var configMap: Map<String, Map<String, Integer>> = emptyMap()
}
