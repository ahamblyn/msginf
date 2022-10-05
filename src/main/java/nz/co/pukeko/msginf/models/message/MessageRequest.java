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

    public MessageRequest(MessageRequestType messageRequestType, MessageType messageType, String connectorName) {
        this.messageRequestType = messageRequestType;
        this.messageType = messageType;
        this.connectorName = connectorName;
    }
}
