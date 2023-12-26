package nz.co.pukekocorp.msginf.models.message;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "The Message type model")
public enum MessageType {
    TEXT,
    BINARY
}
