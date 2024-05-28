package nz.co.pukekocorp.msginf.models.jwt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JWT Request
 */
@Getter
@NoArgsConstructor
@Schema(description = "The JWT Request model")
public class JwtRequest {

    /**
     * The user name.
     */
    @Schema(description = "The user name")
    private String username;

    /**
     * The user's password.
     */
    @Schema(description = "The password")
    private String password;
}
