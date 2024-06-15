package nz.co.pukekocorp.msginf.models.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nz.co.pukekocorp.msginf.models.error.ValidationErrors;

/**
 * Register user response model.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "The Register User response model")
public class UserResponse {
    @Schema(description = "The user name")
    private String userName;

    @Schema(description = "The response message")
    private String message;

    @Schema(description = "The registered user")
    private RegisterUser registerUser;

    @Schema(description = "The validation errors")
    private ValidationErrors validationErrors;
}
