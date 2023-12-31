package nz.co.pukekocorp.msginf.models.configuration;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Submit connection
 * @param submitQueueName the queue to submit the messages t
 * @param submitQueueConnFactoryName the queue connection factory
 * @param requestType the request type: text or binary
 * @param messageTimeToLive the time in ms for the message to live
 * @param messageProperties the properties of the message
 */
@Schema(description = "Submit connection model")
public record SubmitConnection(@Schema(description = "The queue to submit the messages to") String submitQueueName,
                               @Schema(description = "The queue connection factory") String submitQueueConnFactoryName,
                               @Schema(description = "The request type: text or binary") RequestType requestType,
                               @Schema(description = "The time in ms for the message to live") Integer messageTimeToLive,
                               @Schema(description = "The properties of the message") List<MessageProperty> messageProperties) {
}
