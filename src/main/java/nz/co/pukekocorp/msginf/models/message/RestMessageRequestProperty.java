package nz.co.pukekocorp.msginf.models.message;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * REST message request property
 * @param name The property name
 * @param value The property value
 */
@Schema(description = "The Rest Message Property model")
public record RestMessageRequestProperty(@Schema(description = "The property name") String name,
                                         @Schema(description = "The property value") String value) {
}
