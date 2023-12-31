package nz.co.pukekocorp.msginf.models.configuration;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Submit
 * @param submitConnection the submit connection
 * @param connectorName the name of the submit connector
 * @param compressBinaryMessages whether to compress binary connections or not
 */
@Schema(description = "Submit model")
public record Submit(@Schema(description = "The submit connection") SubmitConnection submitConnection,
                     @Schema(description = "The name of the submit connector") String connectorName,
                     @Schema(description = "Whether to compress binary connections or not") Boolean compressBinaryMessages) {
}
