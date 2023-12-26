package nz.co.pukekocorp.msginf.models.message;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "The Message Request type model")
public enum MessageRequestType {
    SUBMIT,
    REQUEST_RESPONSE
}
