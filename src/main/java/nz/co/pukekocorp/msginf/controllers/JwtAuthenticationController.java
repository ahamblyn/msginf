package nz.co.pukekocorp.msginf.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import nz.co.pukekocorp.msginf.infrastructure.security.JwtTokenUtil;
import nz.co.pukekocorp.msginf.models.jwt.JwtError;
import nz.co.pukekocorp.msginf.models.jwt.JwtRequest;
import nz.co.pukekocorp.msginf.models.jwt.JwtResponse;
import nz.co.pukekocorp.msginf.models.user.User;
import nz.co.pukekocorp.msginf.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller to authenticate users.
 */
@Tag(name = "auth", description = "Authentication API")
@RestController
@CrossOrigin
@RequestMapping("/v1/auth")
public class JwtAuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private UserRepository userRepository;

    /**
     * Create a JWT authentication token
     * @param authenticationRequest JWT request
     * @return JWT response
     * @throws Exception throws exception if user cannot be authenticated
     */
    @Operation(
            summary = "Create a JWT authentication token",
            description = "Create a JWT authentication token",
            tags = {"auth"})
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) {
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword()));
            String userName = authentication.getName();
            User user = userRepository.findUserByUserName(userName);
            final String token = jwtTokenUtil.createToken(user);
            return ResponseEntity.ok(new JwtResponse(token));
        } catch (BadCredentialsException e) {
            JwtError jwtError = new JwtError(HttpStatus.BAD_REQUEST, "Invalid username or password");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jwtError);
        } catch (Exception e) {
            JwtError jwtError = new JwtError(HttpStatus.BAD_REQUEST, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jwtError);
        }
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }

}
