package nz.co.pukeko.msginf.models.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class System {
    private JarFiles jarFiles;
    private Queues queues;
    private Connectors connectors;
    private String name;
    private String initialContextFactory;
    private String url;
    private String host;
    private Integer port;
    private String namingFactoryUrlPkgs;
}
