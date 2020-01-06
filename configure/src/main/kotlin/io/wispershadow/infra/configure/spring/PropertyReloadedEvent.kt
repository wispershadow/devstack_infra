package io.wispershadow.infra.configure.spring

import org.springframework.context.ApplicationEvent

class PropertyReloadedEvent(val changedProperties: Map<String, Any>) : ApplicationEvent(changedProperties)