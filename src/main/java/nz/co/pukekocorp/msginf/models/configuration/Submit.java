package nz.co.pukekocorp.msginf.models.configuration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(description = "Submit model")
public class Submit {
    @Schema(description = "The submit connection")
    private SubmitConnection submitConnection;
    @Schema(description = "The name of the submit connector")
    private String connectorName;
    @Schema(description = "Whether to compress binary connections or not")
    private Boolean compressBinaryMessages;
}
