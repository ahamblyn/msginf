package nz.co.pukekocorp.msginf.models.configuration;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * JNDI properties.
 * @param initialContextFactory Initial Context Factory
 * @param url Connection url
 * @param namingFactoryUrlPkgs Naming Factory Url Packages
 * @param vendorJNDIProperties Vendor specific JNDI properties
 */
@Schema(description = "JNDI properties model")
public record JNDIProperties(@Schema(description = "Initial Context Factory") String initialContextFactory,
                             @Schema(description = "Connection url") String url,
                             @Schema(description = "Naming Factory Url Packages") String namingFactoryUrlPkgs,
                             @Schema(description = "Vendor specific JNDI properties") List<VendorJNDIProperty> vendorJNDIProperties) {
}
