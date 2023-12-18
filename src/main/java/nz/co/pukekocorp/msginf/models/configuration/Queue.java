package nz.co.pukekocorp.msginf.models.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Queue {
    private String jndiName;
    private String physicalName;
}
