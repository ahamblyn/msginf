package nz.co.pukekocorp.msginf.models.configuration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@Schema(description = "JNDI properties model")
public class JNDIProperties {
    @Schema(description = "Initial Context Factory")
    private String initialContextFactory;
    @Schema(description = "Connection url")
    private String url;
    @Schema(description = "Naming Factory Url Packages")
    private String namingFactoryUrlPkgs;
    @Schema(description = "Vendor specific JNDI properties")
    private List<VendorJNDIProperty> vendorJNDIProperties;
}
