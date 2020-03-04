package io.wispershadow.infra.akka.spring

import akka.actor.ActorRef
import akka.actor.ActorSystem

class SpringDelegateActorCreatorService(private val actorSystem: ActorSystem) : ActorCreatorService {
    override fun createActor(actorName: String): ActorRef {
        val akkaSpringExtension = AkkaSpringExtension.provider
        val springExt = akkaSpringExtension.get(actorSystem)
        val props = springExt.props(actorName)
        return actorSystem.actorOf(props)
    }
}