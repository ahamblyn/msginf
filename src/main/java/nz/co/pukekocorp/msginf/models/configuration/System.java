package nz.co.pukekocorp.msginf.models.configuration;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * System
 * @param queues List of queues
 * @param topics List of topics
 * @param connectors The connectors
 * @param name Name of the messaging system
 * @param messagingModel The messaging model
 * @param jmsImplementation the JMS implementation: JAVAX_JMS or JAKARTA_JMS
 * @param jndiProperties The JNDI properties
 */
@Schema(description = "System model")
public record System(@Schema(description = "List of queues") List<Destination> queues,
                     @Schema(description = "List of topics") List<Destination> topics,
                     @Schema(description = "The connectors") Connectors connectors,
                     @Schema(description = "Name of the messaging system") String name,
                     @Schema(description = "The messaging model") MessagingModel messagingModel,
                     @Schema(description = "The JMS Implementation") JmsImplementation jmsImplementation,
                     @Schema(description = "The JNDI properties") JNDIProperties jndiProperties) {
}
