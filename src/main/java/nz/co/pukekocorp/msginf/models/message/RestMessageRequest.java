package nz.co.pukekocorp.msginf.models.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * REST message request
 * @param messageSystem The messaging system
 * @param messageConnector The message connector
 * @param textMessage The text message
 * @param binaryMessage The binary message (base64 encoded)
 * @param messageProperties The message properties
 */
@Schema(description = "The Rest Message Request model")
public record RestMessageRequest(@NotBlank(message = "The messaging system is required.") @Schema(description = "The messaging system") String messageSystem,
                                 @NotBlank(message = "The message connector is required.") @Schema(description = "The message connector") String messageConnector,
                                 @Schema(description = "The text message") String textMessage,
                                 @Schema(description = "The binary message (base64 encoded)") String binaryMessage,
                                 @Schema(description = "The message properties") List<RestMessageRequestProperty> messageProperties) {
}
