package nz.co.pukekocorp.msginf.models.configuration;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Publish/Subscribe
 * @param publishSubscribeConnection the publish/subscribe connection
 * @param connectorName the name of the publish/subscribe connector
 * @param compressBinaryMessages whether to compress binary connections or not
 */
@Schema(description = "Publish/Subscribe model")
public record PublishSubscribe(@Schema(description = "The publish/subscribe connection") PublishSubscribeConnection publishSubscribeConnection,
                               @Schema(description = "The name of the submit connector") String connectorName,
                               @Schema(description = "Whether to compress binary connections or not") Boolean compressBinaryMessages) {
}
