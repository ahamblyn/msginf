package nz.co.pukekocorp.msginf.services;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.entities.User;
import nz.co.pukekocorp.msginf.models.user.RegisterUser;
import nz.co.pukekocorp.msginf.models.user.RegisterUserResponse;
import nz.co.pukekocorp.msginf.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class UserService implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Register the new user.
     * @param registerUser the new user
     * @return the response
     */
    @Override
    public RegisterUserResponse registerUser(RegisterUser registerUser) {
        RegisterUserResponse response = new RegisterUserResponse();
        Optional<User> userOpt = userRepository.findByUserName(registerUser.getUserName());
        userOpt.ifPresentOrElse(user -> {
            log.info(user.getUsername() + " user already exists");
            response.setUserName(user.getUsername());
            response.setMessage("User already exists.");
        }, () -> {
            log.info("Creating user");
            User user = new User(registerUser.getUserName(), passwordEncoder.encode(registerUser.getPassword()),
                    registerUser.getFirstName(), registerUser.getLastName());
            userRepository.save(user);
            response.setUserName(user.getUsername());
            response.setMessage("User created successfully.");
        });
        return response;
    }

    /**
     * Delete user.
     * @param registerUser the user
     * @return the response
     */
    @Override
    public RegisterUserResponse deregisterUser(RegisterUser registerUser) {
        RegisterUserResponse response = new RegisterUserResponse();
        Optional<User> userOpt = userRepository.findByUserName(registerUser.getUserName());
        userOpt.ifPresentOrElse(user -> {
            log.info("Deleting user");
            userRepository.delete(user);
            response.setUserName(registerUser.getUserName());
            response.setMessage("User deleted successfully.");
        }, () -> {
            log.info(registerUser.getUserName() + " user doesn't exists");
            response.setUserName(registerUser.getUserName());
            response.setMessage("User doesn't exists.");
        });
        return response;
    }
}