package nz.co.pukekocorp.msginf.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import nz.co.pukekocorp.msginf.models.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * JWT creation and validation
 */
@Component
public class JwtTokenUtil {

    /**
     * Token validity time - 5 hours.
     */
    public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;
    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Create jwt token for user
     * @param user the user
     * @return jwt token
     */
    public String createToken(User user) {
        Claims claims = Jwts.claims().setSubject(user.getUserName());
        claims.put("firstName", user.getFirstName());
        claims.put("lastName", user.getLastName());
        Date tokenCreateTime = new Date();
        Date tokenValidity = new Date(tokenCreateTime.getTime() + TimeUnit.MINUTES.toMillis(JWT_TOKEN_VALIDITY * 1000));
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(tokenValidity)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    /**
     * Resolve the claims using the bearer token.
     * @param request the http request
     * @return the claims
     */
    public Claims resolveClaims(HttpServletRequest request) {
        String token = resolveToken(request);
        if (token != null) {
            return parseJwtClaims(token);
        }
        return null;
    }

    /**
     * Resolve the token.
     * @param request the http request
     * @return the resolved token.
     */
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(TOKEN_HEADER);
        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    /**
     * Validate the claims.
     * @param claims the claim to validate
     * @return validation
     */
    public boolean validateClaims(Claims claims) {
        return claims.getExpiration().after(new Date());
    }

    private Claims parseJwtClaims(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

}
