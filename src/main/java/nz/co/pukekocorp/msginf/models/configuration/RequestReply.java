package nz.co.pukekocorp.msginf.models.configuration;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request reply.
 * @param requestReplyConnection Request-Reply connection
 * @param connectorName the name of the request-reply connector
 * @param compressBinaryMessages whether to compress binary connections or not
 */
@Schema(description = "Request-Reply model")
public record RequestReply(@Schema(description = "Request-Reply connection") RequestReplyConnection requestReplyConnection,
                           @Schema(description = "The name of the request-reply connector") String connectorName,
                           @Schema(description = "Whether to compress binary connections or not") Boolean compressBinaryMessages) {
}
