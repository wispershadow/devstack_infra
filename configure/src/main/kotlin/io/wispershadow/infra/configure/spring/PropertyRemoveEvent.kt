package io.wispershadow.infra.configure.spring

class PropertyRemoveEvent(profileName: String, val removedPropertyNames: Iterable<String>) : PropertyChangeEvent(profileName)