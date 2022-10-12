package nz.co.pukeko.msginf.client.adapter;

import nz.co.pukeko.msginf.models.message.MessageRequest;
import nz.co.pukeko.msginf.models.message.MessageRequestType;
import nz.co.pukeko.msginf.models.message.MessageType;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.UUID;

public class TestUtil {

    public static MessageRequest createTextMessageRequest(MessageRequestType messageRequestType, String connector, String message) {
        String correlationId = UUID.randomUUID().toString();
        MessageRequest messageRequest = new MessageRequest(messageRequestType, connector, correlationId);
        messageRequest.setTextMessage(message);
        return messageRequest;
    }

    public static MessageRequest createBinaryMessageRequest(MessageRequestType messageRequestType, String connector, String filePath) throws Exception {
        String correlationId = UUID.randomUUID().toString();
        MessageRequest messageRequest = new MessageRequest(messageRequestType, connector, correlationId);
        File file = new File(filePath);
        ByteArrayOutputStream message = new ByteArrayOutputStream();
        FileUtils.copyFile(file, message);
        messageRequest.setBinaryMessage(message.toByteArray());
        return messageRequest;
    }
}
