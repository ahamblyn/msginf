package nz.co.pukeko.msginf.models.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestMessageRequest {
    private String messageSystem;
    private String messageConnector;
    private String textMessage;
    // base64 encoded
    private String binaryMessage;
}
