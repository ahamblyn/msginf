package nz.co.pukekocorp.msginf.models.configuration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Vendor JNDI property
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Vendor JNDI properties model")
public class VendorJNDIProperty {
    @Schema(description = "Property name")
    private String name;
    @Schema(description = "Property value")
    private String value;
}
