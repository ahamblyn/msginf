package nz.co.pukeko.msginf.client.adapter;

import nz.co.pukeko.msginf.models.message.MessageRequest;
import nz.co.pukeko.msginf.models.message.MessageRequestType;
import nz.co.pukeko.msginf.models.message.MessageType;

public class TestUtil {

    public static MessageRequest createMessageRequest(MessageRequestType messageRequestType, MessageType messageType,
                                                String connector, String message) {
        MessageRequest messageRequest = new MessageRequest(messageRequestType, messageType, connector);
        messageRequest.setMessage(message);
        return messageRequest;
    }
}
