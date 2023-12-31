package nz.co.pukekocorp.msginf.models.configuration;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Main configuration class.
 * @param systems The messaging systems
 */
@Schema(description = "Configuration model")
public record Configuration(@Schema(description = "The messaging systems") Systems systems) {
}
