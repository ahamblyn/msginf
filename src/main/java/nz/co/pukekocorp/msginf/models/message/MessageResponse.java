package nz.co.pukekocorp.msginf.models.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageResponse {
    private MessageType messageType;
    private String textResponse;
    private byte[] binaryResponse;
    private MessageRequest messageRequest;
}
