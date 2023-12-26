package nz.co.pukekocorp.msginf.models.configuration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Main configuration class.
 */
@Schema(description = "Configuration model")
@Getter
@Setter
@ToString
public class Configuration {
    @Schema(description = "The messaging systems")
    private Systems systems;
}
