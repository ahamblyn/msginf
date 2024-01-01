package nz.co.pukekocorp.msginf.models.jwt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * JWT Response
 */
@Getter
@AllArgsConstructor
@Schema(description = "The JWT Response model")
public class JwtResponse {
    @Schema(description = "The JWT token")
    private final String jwttoken;
}
