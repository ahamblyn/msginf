package nz.co.pukekocorp.msginf.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import nz.co.pukekocorp.msginf.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * JWT creation and validation
 */
@Component
public class JwtTokenUtil {

    @Value("${security.jwt.secret-key}")
    private String secretKey;
    @Value("${security.jwt.authorities.key}")
    private String authoritiesKey;
    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;
    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    /**
     * Create jwt token for user
     * @param user the user
     * @return jwt token
     */
    public String createToken(User user) {
        String authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        Claims claims = Jwts.claims().setSubject(user.getUsername());
        claims.put("firstName", user.getFirstName());
        claims.put("lastName", user.getLastName());
        Date tokenCreateTime = new Date();
        Date tokenValidity = new Date(tokenCreateTime.getTime() + jwtExpiration);
        return Jwts.builder()
                .setClaims(claims)
                .claim(authoritiesKey, authorities)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(tokenValidity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
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

    public Claims parseJwtClaims(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }

    public UsernamePasswordAuthenticationToken getAuthenticationToken(Claims claims) {
        final Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(authoritiesKey).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
        return new UsernamePasswordAuthenticationToken(claims.getSubject(), "", authorities);
    }}
