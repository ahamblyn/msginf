package nz.co.pukekocorp.msginf.models.status;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * The status.
 * @param systemStatusList the system statuses.
 */
@Schema(description = "The Status model")
public record Status(@Schema(description = "The system status list") List<SystemStatus> systemStatusList) {
}
