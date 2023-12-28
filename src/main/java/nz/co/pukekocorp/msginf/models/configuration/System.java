package nz.co.pukekocorp.msginf.models.configuration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * System
 */
@Getter
@Setter
@ToString
@Schema(description = "System model")
public class System {
    @Schema(description = "List of jar file paths")
    private List<String> jarFiles;
    @Schema(description = "List of queues")
    private List<Queue> queues;
    @Schema(description = "The connectors")
    private Connectors connectors;
    @Schema(description = "Name of the messaging system")
    private String name;
    @Schema(description = "The JNDI properties")
    private JNDIProperties jndiProperties;
}
