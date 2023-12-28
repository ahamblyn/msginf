package nz.co.pukekocorp.msginf.models.configuration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Submit connection
 */
@Getter
@Setter
@ToString
@Schema(description = "Submit connection model")
public class SubmitConnection {
    @Schema(description = "The queue to submit the messages to")
    private String submitQueueName;
    @Schema(description = "The queue connection factory")
    private String submitQueueConnFactoryName;
    @Schema(description = "The request type: text or binary")
    private RequestType requestType;
    @Schema(description = "The time in ms for the message to live")
    private Integer messageTimeToLive;
    @Schema(description = "The properties of the message")
    private List<MessageProperty> messageProperties;
}
