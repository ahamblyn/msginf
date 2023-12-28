package nz.co.pukekocorp.msginf.models.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Message response
 */
@Getter
@Setter
@Schema(description = "The Message Response model")
public class MessageResponse {
    @Schema(description = "The message type")
    private MessageType messageType;
    @Schema(description = "The text response")
    private String textResponse;
    @Schema(description = "The binary response")
    private byte[] binaryResponse;
    @Schema(description = "The message request")
    private MessageRequest messageRequest;
}
