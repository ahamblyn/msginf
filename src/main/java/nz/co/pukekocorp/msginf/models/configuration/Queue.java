package nz.co.pukekocorp.msginf.models.configuration;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Queue
 * @param jndiName the JNDI name of the queue
 * @param physicalName the physical name of the queue
 */
@Schema(description = "Queue model")
public record Queue(@Schema(description = "Queue JNDI name") String jndiName,
                    @Schema(description = "Queue physical name") String physicalName) {
}
