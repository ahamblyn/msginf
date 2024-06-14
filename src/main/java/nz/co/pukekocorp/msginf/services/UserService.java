package nz.co.pukekocorp.msginf.services;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.entities.Role;
import nz.co.pukekocorp.msginf.entities.User;
import nz.co.pukekocorp.msginf.models.user.RegisterRole;
import nz.co.pukekocorp.msginf.models.user.RegisterUser;
import nz.co.pukekocorp.msginf.models.user.UserResponse;
import nz.co.pukekocorp.msginf.repositories.RoleRepository;
import nz.co.pukekocorp.msginf.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Manage user service.
 */
@Service
@Slf4j
public class UserService implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Register the new user.
     * @param registerUser the new user
     * @return the user response
     */
    @Override
    public UserResponse registerUser(RegisterUser registerUser) {
        UserResponse response = new UserResponse();
        Optional<User> userOpt = userRepository.findByUserName(registerUser.getUserName());
        userOpt.ifPresentOrElse(user -> {
            log.info(user.getUsername() + " user already exists. Updating accordingly.");
            user.setPassword(passwordEncoder.encode(registerUser.getPassword()));
            user.setFirstName(registerUser.getFirstName());
            user.setLastName(registerUser.getLastName());
            addRoles(registerUser, user);
            userRepository.save(user);
            response.setUserName(user.getUsername());
            response.setRegisterUser(registerUser);
            response.setMessage("User updated");
        }, () -> {
            log.info("Creating new user " + registerUser.getUserName());
            User user = new User(registerUser.getUserName(), passwordEncoder.encode(registerUser.getPassword()),
                    registerUser.getFirstName(), registerUser.getLastName());
            addRoles(registerUser, user);
            userRepository.save(user);
            response.setUserName(user.getUsername());
            response.setRegisterUser(registerUser);
            response.setMessage("User " + user.getUsername() + " created successfully");
        });
        return response;
    }

    private void addRoles(RegisterUser registerUser, User user) {
        Optional.ofNullable(registerUser.getRoles()).ifPresent(roles -> {
            Set<Role> rolesToAdd = new HashSet<>();
            roles.forEach(role -> {
                Optional<Role> optRole = roleRepository.findByName(role.getName());
                optRole.ifPresent(rolesToAdd::add);
            });
            user.setRoles(rolesToAdd);
        });
    }

    /**
     * Delete user.
     * @param userName the user name
     * @return the user response
     */
    @Override
    public UserResponse deregisterUser(String userName) {
        UserResponse response = new UserResponse();
        response.setUserName(userName);
        Optional<User> userOpt = userRepository.findByUserName(userName);
        userOpt.ifPresentOrElse(user -> {
            log.info("Deleting user " + userName);
            userRepository.delete(user);
            response.setMessage("User deregistered successfully");
        }, () -> {
            log.info(userName + " user not found");
            response.setMessage("User not found");
        });
        return response;
    }

    /**
     * Get the user by the user name
     * @param userName the user name
     * @return the user response
     */
    public UserResponse getUserByUserName(String userName) {
        Optional<User> userOpt = userRepository.findByUserName(userName);
        UserResponse response = new UserResponse();
        response.setUserName(userName);
        userOpt.ifPresentOrElse(user -> {
            RegisterUser registerUser = new RegisterUser();
            registerUser.setUserName(user.getUsername());
            registerUser.setFirstName(user.getFirstName());
            registerUser.setLastName(user.getLastName());
            registerUser.setRoles(user.getRoles().stream().map(role -> {
                RegisterRole registerRole = new RegisterRole();
                registerRole.setName(role.getName());
                return registerRole;
            }).toList());
            response.setRegisterUser(registerUser);
            response.setMessage("User retrieved successfully");
        }, () -> {
            response.setMessage("User not found");
        });
        return response;
    }

}
