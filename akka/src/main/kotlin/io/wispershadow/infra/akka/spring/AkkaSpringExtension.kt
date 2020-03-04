package io.wispershadow.infra.akka.spring

import akka.actor.*
import org.springframework.context.ApplicationContext
import org.apache.commons.lang3.RandomStringUtils
import akka.routing.FromConfig

class AkkaSpringExtension : AbstractExtensionId<AkkaSpringExtension.SpringExt>() {
    companion object {
        val provider: AkkaSpringExtension by lazy(LazyThreadSafetyMode.PUBLICATION) {
            AkkaSpringExtension()
        }
    }

    /**
     * Is used by Akka to instantiate the Extension identified by this
     * ExtensionId, internal use only.
     */
    override fun createExtension(system: ExtendedActorSystem): SpringExt {
        return SpringExt(system)
    }

    class SpringExt(private val actorSystem: ActorSystem) : Extension {
        @Volatile
        private lateinit var applicationContext: ApplicationContext

        /**
         * Used to initialize the Spring application context for the extension.
         *
         * @param applicationContext
         */
        fun initialize(applicationContext: ApplicationContext) {
            this.applicationContext = applicationContext
        }

        /**
         * Create a Props for a router to Spring Bean based actors pool. The router will transparently provide access to a pool
         * defined in the application configuration, which contains actors created from Spring Bean definitions.
         *
         * @param actorBeanName Spring Bean name for the actors to be placed in the pool.
         * @return a Props for the router to the Spring Bean based actors.
         */
        fun routerProps(actorBeanName: String): Props {
            val beanProps = props(actorBeanName)
            return FromConfig.getInstance().props(beanProps)
        }

        /**
         * Create a Props for the specified actorBeanName using the SpringActorProducer class.
         *
         * @param actorBeanName The name of the actor bean to create Props for
         * @return a Props that will create the named actor bean using Spring
         */
        fun props(actorBeanName: String): Props {
            return Props.create(SpringActorProducer::class.java, applicationContext, actorBeanName)
        }

        /**
         * Creates an actor that is configured as a Spring Bean
         *
         * @param name name of the spring bean actor
         * @return actor matching the name in the Spring configuration
         */
        fun actorOf(name: String): ActorRef {
            return actorSystem.actorOf(props(name))
        }

        /**
         * Creates an actor that is configured as a Spring Bean
         *
         * @param beanName name of the spring bean actor
         * @param actorName name to give to the actor in the actor system.
         * @return actor matching the name in the Spring configuration
         */
        fun actorOf(beanName: String, actorName: String): ActorRef {
            return actorSystem.actorOf(props(beanName), actorName)
        }

        /**
         * Creates an actor that is configured as a Spring Bean
         *
         * @param beanName name of the spring bean actor
         * @param actorName name to give to the actor in the actor system. However because the actor name should be unique, this
         * is rather a prefix to the actor name. It's guaranteed the actor name will start with this value but the end of the actor
         * name will be random. To match this actor's name in the actor system configuration use wild cards. For example, if you
         * pass (@code "myActor"} in the parameter value, then you can use @{code myActor*} inside the configuration paths.
         * @return actor matching the name in the Spring configuration
         */
        fun uniqueActorOf(beanName: String, actorName: String): ActorRef {
            return actorSystem.actorOf(props(beanName), actorName + "-" + RandomStringUtils.randomAlphabetic(6))
        }
    }
}