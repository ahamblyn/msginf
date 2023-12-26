package nz.co.pukekocorp.msginf.models.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "The Request Type model")
public enum RequestType {
    @JsonProperty("text") TEXT,
    @JsonProperty("binary") BINARY
}
