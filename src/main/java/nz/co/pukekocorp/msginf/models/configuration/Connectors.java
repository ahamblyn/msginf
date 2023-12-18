package nz.co.pukekocorp.msginf.models.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class Connectors {
    private List<Submit> submit = null;
    private List<RequestReply> requestReply = null;
    private Boolean useConnectionPooling;
    private Integer minConnections;
    private Integer maxConnections;
}
