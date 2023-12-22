package nz.co.pukekocorp.msginf.client.adapter;

import nz.co.pukekocorp.msginf.models.message.MessageRequest;
import nz.co.pukekocorp.msginf.models.message.MessageRequestType;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class TestUtil {

    public static MessageRequest createTextMessageRequest(MessageRequestType messageRequestType, String connector, String message) {
        MessageRequest messageRequest = new MessageRequest(messageRequestType, connector);
        messageRequest.setTextMessage(message);
        return messageRequest;
    }

    public static MessageRequest createBinaryMessageRequest(MessageRequestType messageRequestType, String connector, String filePath) throws Exception {
        MessageRequest messageRequest = new MessageRequest(messageRequestType, connector);
        File file = new File(filePath);
        ByteArrayOutputStream message = new ByteArrayOutputStream();
        FileUtils.copyFile(file, message);
        messageRequest.setBinaryMessage(message.toByteArray());
        return messageRequest;
    }
}
