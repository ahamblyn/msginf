package nz.co.pukekocorp.msginf.models.message;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Message type enum
 */
@Schema(description = "The Message type model")
public enum MessageType {

    /**
     * Text message type.
     */
    TEXT,

    /**
     * Binary message type.
     */
    BINARY
}
