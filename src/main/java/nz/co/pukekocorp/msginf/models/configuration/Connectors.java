package nz.co.pukekocorp.msginf.models.configuration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Connectors.
 */
@Getter
@Setter
@ToString
@Schema(description = "Connectors model")
public class Connectors {
    @Schema(description = "List of submit connectors")
    private List<Submit> submit = null;
    @Schema(description = "List of request-reply connectors")
    private List<RequestReply> requestReply = null;
    @Schema(description = "Whether to use queue connection pooling or not")
    private Boolean useConnectionPooling;
    @Schema(description = "The minimum number of queue connections in the pool")
    private Integer minConnections;
    @Schema(description = "The maximum number of queue connections in the pool")
    private Integer maxConnections;
}
