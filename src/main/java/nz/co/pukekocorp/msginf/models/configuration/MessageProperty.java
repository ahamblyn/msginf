package nz.co.pukekocorp.msginf.models.configuration;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Message property.
 * @param name message property name
 * @param value message property value
 */
@Schema(description = "Message properties model")
public record MessageProperty(@Schema(description = "Property name") String name,
                              @Schema(description = "Property value") String value) {
}
