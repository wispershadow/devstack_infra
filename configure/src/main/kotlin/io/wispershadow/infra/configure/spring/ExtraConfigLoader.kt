package io.wispershadow.infra.configure.spring

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.DeferredImportSelector
import org.springframework.core.io.support.SpringFactoriesLoader
import org.springframework.core.type.AnnotationMetadata
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

class ExtraConfigLoader : DeferredImportSelector {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(DeferredImportSelector::class.java)
    }

    override fun selectImports(importingClassMetadata: AnnotationMetadata): Array<String> {
        val classLoader = Thread.currentThread().contextClassLoader
        val names = SpringFactoriesLoader.loadFactoryNames(PropertyReloadExtConfig::class.java, classLoader)
        logger.info("Getting extension property load config: {}", names)
        return names.toTypedArray()
    }
}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
annotation class PropertyReloadExtConfig