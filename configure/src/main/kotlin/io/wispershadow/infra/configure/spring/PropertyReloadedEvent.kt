package io.wispershadow.infra.configure.spring

class PropertyReloadedEvent(profileName: String, val changedProperties: Map<String, Any>) : PropertyChangeEvent(profileName)