package nz.co.pukekocorp.msginf.models.configuration;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * System
 * @param queues List of queues
 * @param connectors The connectors
 * @param name Name of the messaging system
 * @param messagingModel The messaging model
 * @param jndiProperties The JNDI properties
 */
@Schema(description = "System model")
public record System(@Schema(description = "List of queues") List<Queue> queues,
                     @Schema(description = "The connectors") Connectors connectors,
                     @Schema(description = "Name of the messaging system") String name,
                     @Schema(description = "Name of the messaging system") MessagingModel messagingModel,
                     @Schema(description = "The JNDI properties") JNDIProperties jndiProperties) {
}
