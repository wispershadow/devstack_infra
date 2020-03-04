package io.wispershadow.infra.akka

import akka.actor.ActorContext
import akka.actor.ActorRef
import akka.actor.Props
import akka.util.Timeout
import org.slf4j.LoggerFactory

object AkkaUtils {
    private val logger = LoggerFactory.getLogger(AkkaUtils::class.java)

    fun getOrCreateActor(
        actorPath: String,
        context: ActorContext,
        props: Props,
        lookupTimeout: Timeout,
        actorRefConsumer: (ActorRef) -> Any?
    ) {
        logger.debug("Getting or creating actor with path = {}， props = {}", actorPath, props)
        val actorSelection = context.actorSelection(actorPath)
        val actorRefFuture = actorSelection.resolveOne(lookupTimeout)
        actorRefFuture.onComplete({ actorRefTry ->
            actorRefTry.fold({
                logger.info("Creating new actor with path = {}", actorPath)
                val actorRef = context.actorOf(props, actorPath)
                actorRefConsumer(actorRef)
            }, { actorRef ->
                logger.debug("Getting existing actor with path ={}", actorPath)
                actorRefConsumer(actorRef)
            })
        }, context.system().dispatcher())
    }
}