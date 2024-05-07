package nz.co.pukekocorp.msginf.models.configuration;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Connectors.
 * @param submit List of submit connectors
 * @param requestReply List of request-reply connectors
 * @param publishSubscribe List of publish-subscribe connectors
 * @param useConnectionPooling Whether to use queue connection pooling or not
 * @param minConnections The minimum number of queue connections in the pool
 * @param maxConnections The maximum number of queue connections in the pool
 */
@Schema(description = "Connectors model")
public record Connectors(@Schema(description = "List of submit connectors") List<Submit> submit,
                         @Schema(description = "List of request-reply connectors") List<RequestReply> requestReply,
                         @Schema(description = "List of publish-subscribe connectors") List<PublishSubscribe> publishSubscribe,
                         @Schema(description = "Whether to use queue connection pooling or not") Boolean useConnectionPooling,
                         @Schema(description = "The minimum number of queue connections in the pool") Integer minConnections,
                         @Schema(description = "The maximum number of queue connections in the pool") Integer maxConnections,
                         @Schema(description = "Whether to use a durable subscriber or not") Boolean useDurableSubscriber) {
}
