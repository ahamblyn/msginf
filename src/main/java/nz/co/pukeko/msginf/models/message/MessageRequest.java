package nz.co.pukeko.msginf.models.message;

import lombok.Getter;
import lombok.Setter;
import nz.co.pukeko.msginf.infrastructure.data.HeaderProperties;

import java.io.ByteArrayOutputStream;

@Getter
@Setter
public class MessageRequest {
    private MessageRequestType messageRequestType;
    private MessageType messageType;
    private String connectorName;
    private String message;
    private HeaderProperties<String, Object> headerProperties;
    private ByteArrayOutputStream messageStream;
    private String correlationId;

    public MessageRequest(MessageRequestType messageRequestType, MessageType messageType, String connectorName, String correlationId) {
        this.messageRequestType = messageRequestType;
        this.messageType = messageType;
        this.connectorName = connectorName;
        this.correlationId = correlationId;
    }
}
