package nz.co.pukekocorp.msginf.models.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Register user model.
 */
@Getter
@NoArgsConstructor
@Schema(description = "The Register User model")
public class RegisterUser {
    @Schema(description = "The user name")
    private String userName;
    @Schema(description = "The password")
    private String password;
    @Schema(description = "The first name")
    private String firstName;
    @Schema(description = "The last name")
    private String lastName;
    @Schema(description = "The roles")
    private List<RegisterRole> roles;
}
