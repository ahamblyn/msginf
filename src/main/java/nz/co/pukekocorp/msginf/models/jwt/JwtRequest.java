package nz.co.pukekocorp.msginf.models.jwt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JWT Request
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "The JWT Request model")
public class JwtRequest {
    @Schema(description = "The user name")
    private String username;
    @Schema(description = "The password")
    private String password;
}
