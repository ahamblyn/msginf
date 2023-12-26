package nz.co.pukekocorp.msginf.models.configuration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@Schema(description = "Request-Reply connection model")
public class RequestReplyConnection {
    @Schema(description = "The request queue to submit the messages to")
    private String requestQueueName;
    @Schema(description = "The reply queue to receive the reply messages from")
    private String replyQueueName;
    @Schema(description = "The queue connection factory")
    private String requestQueueConnFactoryName;
    @Schema(description = "The request type: text or binary")
    private RequestType requestType;
    @Schema(description = "The time in ms for the message to live")
    private Integer messageTimeToLive;
    @Schema(description = "The time in ms to wait for a reply message")
    private Integer replyWaitTime;
    @Schema(description = "Whether to use a message selector for the reply message")
    private Boolean useMessageSelector;
    @Schema(description = "The properties of the message")
    private List<MessageProperty> messageProperties;
}
