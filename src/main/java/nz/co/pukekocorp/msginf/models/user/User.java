package nz.co.pukekocorp.msginf.models.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "The User model")
public class User {
    @Schema(description = "The user name")
    private String userName;
    @Schema(description = "The password")
    private String password;
    @Schema(description = "The first name")
    private String firstName;
    @Schema(description = "The last name")
    private String lastName;
}
