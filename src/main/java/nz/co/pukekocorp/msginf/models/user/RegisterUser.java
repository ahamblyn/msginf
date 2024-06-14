package nz.co.pukekocorp.msginf.models.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Register user model.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "The Register User model")
public class RegisterUser {

    @NotBlank(message = "The user name is required.")
    @Schema(description = "The user name")
    private String userName;

    @NotBlank(message = "The password is required.")
    @Schema(description = "The password")
    private String password;

    @Schema(description = "The first name")
    private String firstName;

    @Schema(description = "The last name")
    private String lastName;

    @Schema(description = "The roles")
    private List<RegisterRole> roles;
}
