package nz.co.pukekocorp.msginf.models.configuration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Request reply.
 */
@Getter
@Setter
@ToString
@Schema(description = "Request-Reply model")
public class RequestReply {
    @Schema(description = "Request-Reply connection")
    private RequestReplyConnection requestReplyConnection;
    @Schema(description = "The name of the request-reply connector")
    private String connectorName;
    @Schema(description = "Whether to compress binary connections or not")
    private Boolean compressBinaryMessages;
}
