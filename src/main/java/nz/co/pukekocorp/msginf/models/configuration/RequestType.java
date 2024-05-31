package nz.co.pukekocorp.msginf.models.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request type enum
 */
@Schema(description = "The Request Type model")
public enum RequestType {

    /**
     * Text request type.
     */
    @JsonProperty("text") TEXT,

    /**
     * Binary request type.
     */
    @JsonProperty("binary") BINARY
}
