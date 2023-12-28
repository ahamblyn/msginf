package nz.co.pukekocorp.msginf.models.configuration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Message property.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Message properties model")
public class MessageProperty {
    @Schema(description = "Property name")
    private String name;
    @Schema(description = "Property value")
    private String value;
}
