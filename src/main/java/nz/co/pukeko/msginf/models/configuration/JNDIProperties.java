package nz.co.pukeko.msginf.models.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class JNDIProperties {
    private String initialContextFactory;
    private String url;
    private String namingFactoryUrlPkgs;
    private List<VendorJNDIProperty> vendorJNDIProperties;
}
