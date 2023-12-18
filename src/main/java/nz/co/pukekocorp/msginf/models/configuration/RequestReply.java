package nz.co.pukekocorp.msginf.models.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RequestReply {
    private RequestReplyConnection requestReplyConnection;
    private String connectorName;
    private Boolean compressBinaryMessages;
}
