package nz.co.pukekocorp.msginf.models.statistics;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * The system stats.
 * @param messagingSystem the messaging system.
 * @param connectorStatsList the connector statistics.
 */
@Schema(description = "The System Statistics model")
public record SystemStats(@Schema(description = "The messaging system") String messagingSystem,
                          @Schema(description = "The connector statistics list") List<ConnectorStats> connectorStatsList) {
}
