package nz.co.pukekocorp.msginf.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import nz.co.pukekocorp.msginf.models.user.RegisterUser;
import nz.co.pukekocorp.msginf.models.user.UserResponse;
import nz.co.pukekocorp.msginf.services.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller to manage users.
 */
@Tag(name = "user", description = "User API")
@RestController
@RequestMapping("/v1/user")
public class UserController {

    @Autowired
    private IUserService userService;

    /**
     * Create a new user
     * @param registerUser user request
     * @return user response
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create a new user",
            description = "Create a new user",
            tags = {"user"})
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterUser registerUser) {
        try {
            UserResponse response = userService.registerUser(registerUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            UserResponse response = new UserResponse();
            response.setUserName(registerUser.getUserName());
            response.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Delete user
     * @param userName user name
     * @return user response
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete a user",
            description = "Delete a user",
            tags = {"user"})
    @RequestMapping(value = "/deregister/{userName}", method = RequestMethod.POST)
    public ResponseEntity<UserResponse> deregisterUser(@NotBlank @PathVariable("userName") String userName) {
        try {
            UserResponse response = userService.deregisterUser(userName);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            UserResponse response = new UserResponse();
            response.setUserName(userName);
            response.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Get the user
     * @param userName the user name
     * @return user response
     */
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Get user",
            description = "Get user",
            tags = {"user"})
    @GetMapping(value = "/{userName}")
    public ResponseEntity<UserResponse> getUser(@NotBlank @PathVariable("userName") String userName) {
        UserResponse response = userService.getUserByUserName(userName);
        return ResponseEntity.ok(response);
    }
}
