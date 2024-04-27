package nz.co.pukekocorp.msginf.models.configuration;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Destination
 * @param jndiName the JNDI name of the destination
 * @param physicalName the physical name of the destination
 */
@Schema(description = "Destination model")
public record Destination(@Schema(description = "Destination JNDI name") String jndiName,
                          @Schema(description = "Destination physical name") String physicalName) {
}
