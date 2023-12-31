package nz.co.pukekocorp.msginf.models.configuration;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Request reply connection
 * @param requestQueueName the request queue to submit the messages to
 * @param replyQueueName the reply queue to receive the reply messages from
 * @param requestQueueConnFactoryName the queue connection factory
 * @param requestType the request type: text or binary
 * @param messageTimeToLive the time in ms for the message to live
 * @param replyWaitTime the time in ms to wait for a reply message
 * @param useMessageSelector whether to use a message selector for the reply message
 * @param messageProperties the properties of the message
 */
@Schema(description = "Request-Reply connection model")
public record RequestReplyConnection(@Schema(description = "The request queue to submit the messages to") String requestQueueName,
                                     @Schema(description = "The reply queue to receive the reply messages from") String replyQueueName,
                                     @Schema(description = "The queue connection factory") String requestQueueConnFactoryName,
                                     @Schema(description = "The request type: text or binary") RequestType requestType,
                                     @Schema(description = "The time in ms for the message to live") Integer messageTimeToLive,
                                     @Schema(description = "The time in ms to wait for a reply message") Integer replyWaitTime,
                                     @Schema(description = "Whether to use a message selector for the reply message") Boolean useMessageSelector,
                                     @Schema(description = "The properties of the message") List<MessageProperty> messageProperties) {
}
