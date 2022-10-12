package nz.co.pukeko.msginf.models.message;

import lombok.Getter;
import lombok.Setter;
import nz.co.pukeko.msginf.models.configuration.MessageProperty;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Getter
@Setter
public class MessageRequest {
    private MessageRequestType messageRequestType;
    private MessageType messageType;
    private String connectorName;
    private String message;
    private List<MessageProperty> messageProperties;
    private ByteArrayOutputStream messageStream;
    private String correlationId;

    public MessageRequest(MessageRequestType messageRequestType, String connectorName, String correlationId) {
        this.messageRequestType = messageRequestType;
        this.connectorName = connectorName;
        this.correlationId = correlationId;
    }
}
