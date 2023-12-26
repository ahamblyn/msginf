package nz.co.pukekocorp.msginf.models.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(description = "The Rest Message Property model")
public class RestMessageRequestProperty {
    @Schema(description = "The property name")
    private String name;
    @Schema(description = "The property value")
    private String value;
}
