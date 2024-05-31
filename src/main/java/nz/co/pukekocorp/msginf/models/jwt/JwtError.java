package nz.co.pukekocorp.msginf.models.jwt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * The JWT error.
 */
@Getter
@AllArgsConstructor
@Schema(description = "The JWT Error model")
public class JwtError {

    /**
     * The HTTP status of the JWT error.
     */
    @Schema(description = "The JWT error HTTP status")
    private HttpStatus httpStatus;

    /**
     * The JWT error message.
     */
    @Schema(description = "The JWT error message")
    private String message;
}
