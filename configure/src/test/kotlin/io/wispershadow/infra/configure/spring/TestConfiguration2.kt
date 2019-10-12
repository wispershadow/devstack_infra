package io.wispershadow.infra.configure.spring

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class TestConfiguration2 {
    @Value("\${core.additional.value1}")
    var valueProperty1: String? = null
}