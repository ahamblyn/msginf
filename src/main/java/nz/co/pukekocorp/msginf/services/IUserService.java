package nz.co.pukekocorp.msginf.services;

import nz.co.pukekocorp.msginf.models.user.RegisterUser;
import nz.co.pukekocorp.msginf.models.user.RegisterUserResponse;

public interface IUserService {

    /**
     * Register the new user.
     * @param registerUser the new user
     * @return the response
     */
    RegisterUserResponse registerUser(RegisterUser registerUser);

    /**
     * Delete user.
     * @param registerUser the user
     * @return the response
     */
    RegisterUserResponse deregisterUser(RegisterUser registerUser);
}
