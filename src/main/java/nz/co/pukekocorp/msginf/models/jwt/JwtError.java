package nz.co.pukekocorp.msginf.models.jwt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
@Schema(description = "The JWT Error model")
public class JwtError {
    @Schema(description = "The JWT error HTTP status")
    private HttpStatus httpStatus;
    @Schema(description = "The JWT error message")
    private String message;
}
