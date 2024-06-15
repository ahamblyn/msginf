package nz.co.pukekocorp.msginf.models.jwt;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "The user name is required.")
    @Schema(description = "The user name")
    private String username;

    /**
     * The user's password.
     */
    @NotBlank(message = "The password is required.")
    @Schema(description = "The password")
    private String password;
}
