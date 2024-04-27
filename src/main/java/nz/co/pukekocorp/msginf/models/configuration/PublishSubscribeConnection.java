package nz.co.pukekocorp.msginf.models.configuration;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Publish Subscribe connection
 * @param publishSubscribeTopicName the topic to publish/subscribe the messages to/from
 * @param publishSubscribeTopicConnFactoryName the topic connection factory
 * @param requestType the request type: text or binary
 * @param messageTimeToLive the time in ms for the message to live
 * @param messageProperties the properties of the message
 */
@Schema(description = "Publish Subscribe connection model")
public record PublishSubscribeConnection(@Schema(description = "The topic to publish/subscribe the messages to/from") String publishSubscribeTopicName,
                                         @Schema(description = "The topic connection factory") String publishSubscribeTopicConnFactoryName,
                                         @Schema(description = "The request type: text or binary") RequestType requestType,
                                         @Schema(description = "The time in ms for the message to live") Integer messageTimeToLive,
                                         @Schema(description = "The properties of the message") List<MessageProperty> messageProperties) {
}
