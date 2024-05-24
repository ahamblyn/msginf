package nz.co.pukekocorp.msginf.models.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Register user response model.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "The Register User response model")
public class RegisterUserResponse {
    private String userName;
    private String message;
}
