package nz.co.pukekocorp.msginf.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import nz.co.pukekocorp.msginf.models.user.RegisterUser;
import nz.co.pukekocorp.msginf.models.user.RegisterUserResponse;
import nz.co.pukekocorp.msginf.services.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
     * @return JWT response
     */
    @Operation(
            summary = "Create a new user",
            description = "Create a new user",
            tags = {"user"})
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<?> registerUser(@RequestBody RegisterUser registerUser) {
        try {
            RegisterUserResponse response = userService.registerUser(registerUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            RegisterUserResponse response = new RegisterUserResponse();
            response.setUserName(registerUser.getUserName());
            response.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Delete user
     * @param registerUser user request
     * @return JWT response
     */
    @Operation(
            summary = "Delete user",
            description = "Delete user",
            tags = {"user"})
    @RequestMapping(value = "/deregister", method = RequestMethod.POST)
    public ResponseEntity<?> deregisterUser(@RequestBody RegisterUser registerUser) {
        try {
            RegisterUserResponse response = userService.deregisterUser(registerUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            RegisterUserResponse response = new RegisterUserResponse();
            response.setUserName(registerUser.getUserName());
            response.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
