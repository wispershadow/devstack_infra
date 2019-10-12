package io.wispershadow.infra.configure.spring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder

@SpringBootApplication
class MainAppl {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            System.setProperty("spring.profiles.active", "local")
            SpringApplicationBuilder(TestConfiguration1::class.java, TestConfiguration2::class.java, CustomPropertiesConfig::class.java)
                .run(*args)

        }
    }
}