package io.wispershadow.infra.akka.spring

import akka.actor.ActorRef

interface ActorCreatorService {
    fun createActor(actorName: String): ActorRef
}