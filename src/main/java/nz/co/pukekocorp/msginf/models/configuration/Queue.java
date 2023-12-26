package nz.co.pukekocorp.msginf.models.configuration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(description = "Queue model")
public class Queue {
    @Schema(description = "Queue JNDI name")
    private String jndiName;
    @Schema(description = "Queue physical name")
    private String physicalName;
}
