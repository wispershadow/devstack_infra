package io.wispershadow.infra.configure.spring.ext.zk

import io.wispershadow.infra.configure.spring.ReloadablePropertySourceBuilder
import org.springframework.boot.SpringApplication
import org.springframework.core.env.Environment

class ZkReloadablePropertySourceBuilder : ReloadablePropertySourceBuilder {

    override fun build(): Map<String, Any> {
        return mapOf()
    }
}