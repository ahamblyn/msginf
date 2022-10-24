package nz.co.pukeko.msginf.models.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class JNDIProperties {
    private String initialContextFactory;
    private String url;
    private String host;
    private Integer port;
    private String namingFactoryUrlPkgs;
}
