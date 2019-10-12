package io.wispershadow.infra.akka.spring

import akka.actor.ActorSystem
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
//note: all actor beans in spring config must be of scope prototype
class ActorSystemConfig {
    companion object {
        private const val ACTOR_SYSTEM_NAME = "actorSystem"
    }
    @Bean
    fun actorSystemFactory(): ActorSystemFactory {
        return ActorSystemFactory()
    }

    @Bean
    fun actorSystem(actorSystemFactory: ActorSystemFactory): ActorSystem {
        return actorSystemFactory.createActorSystem(ACTOR_SYSTEM_NAME)
    }

    @Bean
    fun actorCreatorService(actorSystem: ActorSystem): ActorCreatorService {
        return SpringDelegateActorCreatorService(actorSystem)
    }

    @Bean
    fun akkaServiceFactory(defaultTimeout: Long, actorCreatorService: ActorCreatorService): AkkaServiceFactory {
        return AkkaServiceFactory(defaultTimeout, actorCreatorService)
    }

}