package nz.co.pukekocorp.msginf.models.message;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Message request type enum.
 */
@Schema(description = "The Message Request type model")
public enum MessageRequestType {
    SUBMIT,
    REQUEST_RESPONSE
}
