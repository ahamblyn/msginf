package nz.co.pukekocorp.msginf.models.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Messaging model enum
 */
@Schema(description = "The JMS Implementation model")
public enum JmsImplementation {
    @JsonProperty("javax-jms") JAVAX_JMS,
    @JsonProperty("jakarta-jms") JAKARTA_JMS
}
