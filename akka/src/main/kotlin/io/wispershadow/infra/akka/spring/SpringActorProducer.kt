package io.wispershadow.infra.akka.spring

import akka.actor.Actor
import akka.actor.IndirectActorProducer
import org.springframework.context.ApplicationContext

class SpringActorProducer(private val applicationContext: ApplicationContext,
                          private val beanName: String): IndirectActorProducer {
    override fun actorClass(): Class<out Actor> {
        return applicationContext.getType(beanName) as Class<out Actor>
    }

    override fun produce(): Actor {
        return applicationContext.getBean(beanName) as Actor
    }

}