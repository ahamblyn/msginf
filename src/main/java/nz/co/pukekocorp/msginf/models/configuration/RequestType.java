package nz.co.pukekocorp.msginf.models.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum RequestType {
    @JsonProperty("text") TEXT,
    @JsonProperty("binary") BINARY
}
