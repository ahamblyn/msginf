package nz.co.pukekocorp.msginf.models.status;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The connector status.
 * @param messageConnector the message connector.
 * @param valid whether the message connector is valid or not.
 */
@Schema(description = "The Connector Status model")
public record ConnectorStatus(@Schema(description = "The message connector") String messageConnector,
                              @Schema(description = "Whether the message connector is valid or not") boolean valid) {
}
