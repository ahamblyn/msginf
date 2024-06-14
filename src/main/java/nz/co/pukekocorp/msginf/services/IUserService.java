package nz.co.pukekocorp.msginf.services;

import nz.co.pukekocorp.msginf.models.user.RegisterUser;
import nz.co.pukekocorp.msginf.models.user.UserResponse;

/**
 * Manage user service.
 */
public interface IUserService {

    /**
     * Register the new user.
     * @param registerUser the new user
     * @return the user response
     */
    UserResponse registerUser(RegisterUser registerUser);

    /**
     * Delete user.
     * @param userName the user name
     * @return the user response
     */
    UserResponse deregisterUser(String userName);

    /**
     * Get the user by the user name
     * @param userName the user name
     * @return the user response
     */
    UserResponse getUserByUserName(String userName);
}
