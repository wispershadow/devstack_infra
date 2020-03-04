package io.wispershadow.infra.akka.spring

import akka.pattern.Patterns
import akka.util.Timeout
import scala.compat.java8.FutureConverters
import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeUnit

class AkkaServiceFactory(
    private val defaultTimeout: Long,
    private val actorCreatorService: ActorCreatorService
) {
    fun <T> invokeAkkaServiceAsync(actorName: String, vararg args: Any): CompletionStage<T> {
        val actorRef = actorCreatorService.createActor(actorName)
        val future = Patterns.ask(actorRef, args, Timeout(defaultTimeout, TimeUnit.MILLISECONDS))
        return FutureConverters.toJava(future) as CompletionStage<T>
    }
}