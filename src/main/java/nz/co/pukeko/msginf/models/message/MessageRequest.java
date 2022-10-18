package nz.co.pukeko.msginf.models.message;

import lombok.Getter;
import lombok.Setter;
import nz.co.pukeko.msginf.models.configuration.MessageProperty;

import java.util.List;

@Getter
@Setter
public class MessageRequest {
    private MessageRequestType messageRequestType;
    private MessageType messageType;
    private String connectorName;
    private String textMessage;
    private List<MessageProperty> messageProperties;
    private byte[] binaryMessage;
    private String correlationId;

    public MessageRequest(MessageRequestType messageRequestType, String connectorName, String correlationId) {
        this.messageRequestType = messageRequestType;
        this.connectorName = connectorName;
        this.correlationId = correlationId;
    }
}
