package nz.co.pukeko.msginf.models.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageResponse {
    private MessageResponseType messageResponseType;
    private String textResponse;
    private byte[] binaryResponse;
    private MessageRequest messageRequest;
}
