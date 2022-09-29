package nz.co.pukeko.msginf.models.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Submit {
    private SubmitConnection submitConnection;
    private String connectorName;
    private Boolean compressBinaryMessages;
}
