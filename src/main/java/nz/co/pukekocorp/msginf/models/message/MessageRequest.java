package nz.co.pukekocorp.msginf.models.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import nz.co.pukekocorp.msginf.models.configuration.MessageProperty;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "The Message Request model")
public class MessageRequest {
    @Schema(description = "Message request type")
    private MessageRequestType messageRequestType;
    @Schema(description = "Message type")
    private MessageType messageType;
    @Schema(description = "Connector name")
    private String connectorName;
    @Schema(description = "Text message data")
    private String textMessage;
    @Schema(description = "Message properties")
    private List<MessageProperty> messageProperties;
    @Schema(description = "Binary message data")
    private byte[] binaryMessage;
    @Schema(description = "Correlation id")
    private String correlationId;

    public MessageRequest(MessageRequestType messageRequestType, String connectorName, String correlationId) {
        this.messageRequestType = messageRequestType;
        this.connectorName = connectorName;
        this.correlationId = correlationId;
    }

    public MessageRequest(MessageRequestType messageRequestType, String connectorName) {
        this.messageRequestType = messageRequestType;
        this.connectorName = connectorName;
        this.correlationId = UUID.randomUUID().toString();
    }
}
