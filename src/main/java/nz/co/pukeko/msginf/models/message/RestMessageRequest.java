package nz.co.pukeko.msginf.models.message;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RestMessageRequest {
    private String messageSystem;
    private String messageConnector;
    private String textMessage;
    // base64 encoded
    private String binaryMessage;
    private List<RestMessageRequestProperty> messageProperties;
}
