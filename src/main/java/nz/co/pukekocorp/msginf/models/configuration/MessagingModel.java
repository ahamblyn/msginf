package nz.co.pukekocorp.msginf.models.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Messaging model enum
 */
@Schema(description = "The Messaging Model model")
public enum MessagingModel {

    /**
     * Point-to-point messaging model.
     */
    @JsonProperty("point-to-point") POINT_TO_POINT,

    /**
     * Publish-subscribe messaging model.
     */
    @JsonProperty("publish-subscribe") PUBLISH_SUBSCRIBE
}
