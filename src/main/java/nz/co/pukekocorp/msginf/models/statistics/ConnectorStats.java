package nz.co.pukekocorp.msginf.models.statistics;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The connector statistics.
 * @param messageConnector the message connector.
 * @param messagesSent the number of messages sent.
 * @param failedMessagesSent the number of failed messages sent.
 * @param averageMessageTime the average message time (ms).
 * @param medianMessageTime the median message time (ms).
 * @param maximumMessageTime the maximum message time (ms).
 * @param minimumMessageTime the minimum message time (ms).
 * @param standardDeviationMessageTime the standard deviation message time (ms).
 */
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
