package nz.co.pukekocorp.msginf.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import nz.co.pukekocorp.msginf.entities.User;
import nz.co.pukekocorp.msginf.models.user.RegisterUser;
import nz.co.pukekocorp.msginf.models.user.UserResponse;
import nz.co.pukekocorp.msginf.services.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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
    @Operation(
            summary = "Create a new user",
            description = "Create a new user",
            tags = {"user"})
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<?> registerUser(@RequestBody RegisterUser registerUser) {
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
     * @param registerUser user request
     * @return user response
     */
    @Operation(
            summary = "Delete user",
            description = "Delete user",
            tags = {"user"})
    @RequestMapping(value = "/deregister", method = RequestMethod.POST)
    public ResponseEntity<?> deregisterUser(@RequestBody RegisterUser registerUser) {
        try {
            UserResponse response = userService.deregisterUser(registerUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            UserResponse response = new UserResponse();
            response.setUserName(registerUser.getUserName());
            response.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Get the user
     * @param userName the user name
     * @return user response
     */
    @Operation(
            summary = "Delete user",
            description = "Delete user",
            tags = {"user"})
    @GetMapping(value = "/{userName}")
    public ResponseEntity<?> getUser(@NotBlank @PathVariable("userName") String userName) {
        Optional<User> userOpt = userService.getUserByUserName(userName);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword("******");
            return ResponseEntity.ok(user);
        } else {
            UserResponse response = new UserResponse();
            response.setUserName(userName);
            response.setMessage("User not found.");
            return ResponseEntity.ok(response);
        }
    }
}
