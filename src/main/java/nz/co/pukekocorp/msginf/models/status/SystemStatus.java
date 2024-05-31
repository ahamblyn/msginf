package nz.co.pukekocorp.msginf.models.status;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * The system status.
 * @param messagingSystem the messaging system.
 * @param connectorStatusList the connector statuses.
 */
@Schema(description = "The System Status model")
public record SystemStatus(@Schema(description = "The messaging system") String messagingSystem,
                           @Schema(description = "The connector status list") List<ConnectorStatus> connectorStatusList) {
}
