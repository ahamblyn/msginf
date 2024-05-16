package nz.co.pukekocorp.msginf.models.statistics;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "The Connector Statistics model")
public record ConnectorStats(@Schema(description = "The message connector") String messageConnector,
                             @Schema(description = "The number of messages sent") double messagesSent,
                             @Schema(description = "The number of failed messages sent") double failedMessagesSent,
                             @Schema(description = "The average message time (ms)") double averageMessageTime,
                             @Schema(description = "The median message time (ms)") double medianMessageTime,
                             @Schema(description = "The maximum message time (ms)") double maximumMessageTime,
                             @Schema(description = "The minimum message time (ms)") double minimumMessageTime,
                             @Schema(description = "The standard deviation message time (ms)") double standardDeviationMessageTime) {
}
