package nz.co.pukekocorp.msginf.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import nz.co.pukekocorp.msginf.entities.User;
import nz.co.pukekocorp.msginf.infrastructure.security.JwtTokenUtil;
import nz.co.pukekocorp.msginf.models.jwt.JwtError;
import nz.co.pukekocorp.msginf.models.jwt.JwtRequest;
import nz.co.pukekocorp.msginf.models.jwt.JwtResponse;
import nz.co.pukekocorp.msginf.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST Controller to authenticate users.
 */
@Tag(name = "auth", description = "Authentication API")
@RestController
@CrossOrigin
@RequestMapping("/v1/auth")
public class JwtAuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;

    /**
     * Create the JwtAuthenticationController
     * @param authenticationManager the authentication manager.
     * @param jwtTokenUtil the JWT token util.
     * @param userRepository the user repository.
     */
    public JwtAuthenticationController(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;
    }

    /**
     * Create a JWT authentication token
     * @param authenticationRequest JWT request
     * @return JWT response
     */
    @Operation(
            summary = "Create a JWT authentication token",
            description = "Create a JWT authentication token",
            tags = {"auth"})
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@Valid @RequestBody JwtRequest authenticationRequest) {
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword()));
            String userName = authentication.getName();
            Optional<User> userOpt = userRepository.findByUserName(userName);
            User user = userOpt.orElseThrow(() -> new RuntimeException("User " + userName + " not found."));
            final String token = jwtTokenUtil.createToken(user);
            return ResponseEntity.ok(new JwtResponse(token));
        } catch (BadCredentialsException e) {
            JwtError jwtError = new JwtError(HttpStatus.BAD_REQUEST, "Invalid username or password", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jwtError);
        } catch (Exception e) {
            JwtError jwtError = new JwtError(HttpStatus.BAD_REQUEST, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jwtError);
        }
    }
}
