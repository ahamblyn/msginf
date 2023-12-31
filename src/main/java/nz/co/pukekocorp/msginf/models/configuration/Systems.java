package nz.co.pukekocorp.msginf.models.configuration;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Systems
 * @param system List of messaging systems
 */
@Schema(description = "Systems model")
public record Systems(@Schema(description = "List of messaging systems") List<System> system) {
}
