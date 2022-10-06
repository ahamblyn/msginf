package nz.co.pukeko.msginf.client.adapter;

import nz.co.pukeko.msginf.models.message.MessageRequest;
import nz.co.pukeko.msginf.models.message.MessageRequestType;
import nz.co.pukeko.msginf.models.message.MessageType;

import java.util.UUID;

public class TestUtil {

    public static MessageRequest createMessageRequest(MessageRequestType messageRequestType, MessageType messageType,
                                                String connector, String message) {
        String correlationId = UUID.randomUUID().toString();
        MessageRequest messageRequest = new MessageRequest(messageRequestType, messageType, connector, correlationId);
        messageRequest.setMessage(message);
        return messageRequest;
    }
}
