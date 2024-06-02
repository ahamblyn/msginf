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
     * @return the response
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
            // add roles if required
            List<RegisterRole> rolesToAdd = new ArrayList<>();
            Optional.ofNullable(registerUser.getRoles()).ifPresent(roles -> {
                Optional.ofNullable(user.getRoles()).ifPresent(userRoles -> {
                    roles.forEach(role -> {
                        if (userRoles.stream().noneMatch(userRole -> userRole.getName().equals(role.getName()))) {
                            rolesToAdd.add(role);
                        }
                    });
                });
            });
            rolesToAdd.forEach(role -> {
                Optional<Role> optRole = roleRepository.findByName(role.getName());
                optRole.ifPresent(dbRole -> user.getRoles().add(dbRole));
            });
            userRepository.save(user);
            response.setUserName(user.getUsername());
            response.setMessage("User updated.");
        }, () -> {
            log.info("Creating new user " + registerUser.getUserName());
            User user = new User(registerUser.getUserName(), passwordEncoder.encode(registerUser.getPassword()),
                    registerUser.getFirstName(), registerUser.getLastName());
            // add roles
            Optional.ofNullable(registerUser.getRoles()).ifPresent(roles -> {
                Set<Role> rolesToAdd = new HashSet<>();
                roles.forEach(role -> {
                    Optional<Role> optRole = roleRepository.findByName(role.getName());
                    optRole.ifPresent(rolesToAdd::add);
                });
                user.setRoles(rolesToAdd);
            });
            userRepository.save(user);
            response.setUserName(user.getUsername());
            response.setMessage("User " + user.getUsername() + " created successfully.");
        });
        return response;
    }

    /**
     * Delete user.
     * @param registerUser the user
     * @return the response
     */
    @Override
    public UserResponse deregisterUser(RegisterUser registerUser) {
        UserResponse response = new UserResponse();
        Optional<User> userOpt = userRepository.findByUserName(registerUser.getUserName());
        userOpt.ifPresentOrElse(user -> {
            log.info("Deleting user " + registerUser.getUserName());
            userRepository.delete(user);
            response.setUserName(registerUser.getUserName());
            response.setMessage("User deleted successfully.");
        }, () -> {
            log.info(registerUser.getUserName() + " user doesn't exist");
            response.setUserName(registerUser.getUserName());
            response.setMessage("User doesn't exists.");
        });
        return response;
    }

    /**
     * Get the user by the user name
     * @param userName the user name
     * @return the user
     */
    public Optional<User> getUserByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }

}
