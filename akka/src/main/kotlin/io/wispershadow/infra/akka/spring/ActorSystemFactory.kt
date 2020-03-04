package io.wispershadow.infra.akka.spring

import akka.actor.ActorSystem
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

class ActorSystemFactory : ApplicationContextAware {
    private lateinit var applicationContext: ApplicationContext

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    fun createActorSystem(systemName: String): ActorSystem {
        val system = ActorSystem.create(systemName)
        AkkaSpringExtension.provider.get(system).initialize(applicationContext)
        return system
    }
}