package nz.co.pukekocorp.msginf.models.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class System {
    private List<String> jarFiles;
    private List<Queue> queues;
    private Connectors connectors;
    private String name;
    private JNDIProperties jndiProperties;
}
