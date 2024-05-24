package nz.co.pukekocorp.msginf.services;

import nz.co.pukekocorp.msginf.entities.User;
import nz.co.pukekocorp.msginf.models.user.RegisterUser;
import nz.co.pukekocorp.msginf.models.user.UserResponse;

import java.util.Optional;

public interface IUserService {

    /**
     * Register the new user.
     * @param registerUser the new user
     * @return the response
     */
    UserResponse registerUser(RegisterUser registerUser);

    /**
     * Delete user.
     * @param registerUser the user
     * @return the response
     */
    UserResponse deregisterUser(RegisterUser registerUser);

    /**
     * Get the user by the user name
     * @param userName the user name
     * @return the user
     */
    Optional<User> getUserByUserName(String userName);
}
