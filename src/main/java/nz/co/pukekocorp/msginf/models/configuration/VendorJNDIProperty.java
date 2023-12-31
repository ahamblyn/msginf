package nz.co.pukekocorp.msginf.models.configuration;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Vendor JNDI property
 * @param name Vendor JNDI property name
 * @param value Vendor JNDI property value
 */
@Schema(description = "Vendor JNDI properties model")
public record VendorJNDIProperty(@Schema(description = "Property name") String name,
                                 @Schema(description = "Property value") String value) {
}
