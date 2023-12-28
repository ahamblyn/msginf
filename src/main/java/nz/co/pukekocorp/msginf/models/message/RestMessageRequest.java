package nz.co.pukekocorp.msginf.models.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * REST message request
 */
@Getter
@Setter
@Schema(description = "The Rest Message Request model")
public class RestMessageRequest {
    @Schema(description = "The messaging system")
    private String messageSystem;
    @Schema(description = "The message connector")
    private String messageConnector;
    @Schema(description = "The text message")
    private String textMessage;
    // base64 encoded
    @Schema(description = "The binary message (base64 encoded)")
    private String binaryMessage;
    @Schema(description = "The message properties")
    private List<RestMessageRequestProperty> messageProperties;
}
