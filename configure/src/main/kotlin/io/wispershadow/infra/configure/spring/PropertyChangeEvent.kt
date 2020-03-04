package io.wispershadow.infra.configure.spring

import org.springframework.context.ApplicationEvent

open class PropertyChangeEvent(val propertySourceKey: String) : ApplicationEvent(propertySourceKey)