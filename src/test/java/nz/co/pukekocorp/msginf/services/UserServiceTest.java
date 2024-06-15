/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.services;

import nz.co.pukekocorp.msginf.entities.Role;
import nz.co.pukekocorp.msginf.entities.User;
import nz.co.pukekocorp.msginf.models.user.RegisterRole;
import nz.co.pukekocorp.msginf.models.user.RegisterUser;
import nz.co.pukekocorp.msginf.models.user.UserResponse;
import nz.co.pukekocorp.msginf.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    public void testFindByUserName() {
        // Given
        UserResponse response = new UserResponse();
        response.setUserName("fred");

        RegisterUser registerUser = new RegisterUser();
        registerUser.setUserName("fred");
        registerUser.setFirstName("Fred");
        registerUser.setLastName("Dagg");
        registerUser.setPassword("qwerty123");
        registerUser.setUserName("fred");

        RegisterRole role = new RegisterRole();
        role.setName("USER");

        registerUser.setRoles(List.of(role));
        response.setRegisterUser(registerUser);

        User user = new User();
        user.setId(1);
        user.setUserName("fred");
        user.setPassword("qwerty123");
        user.setFirstName("Fred");
        user.setLastName("Dagg");

        Role userRole = new Role();
        userRole.setId(1);
        userRole.setName("USER");
        userRole.setDescription("User role");

        user.setRoles(Set.of(userRole));

        // When
        when(userRepository.findByUserName("fred")).thenReturn(Optional.of(user));
        UserResponse returnedResponse = this.userService.getUserByUserName("fred");

        // Then
        assertNotNull(returnedResponse);
        assertEquals("fred", returnedResponse.getUserName());
        RegisterUser returnedRegisterUser = returnedResponse.getRegisterUser();
        assertEquals("fred", returnedRegisterUser.getUserName());
        assertEquals("Fred", returnedRegisterUser.getFirstName());
        assertEquals("Dagg", returnedRegisterUser.getLastName());
        List<RegisterRole> returnedRoles = returnedRegisterUser.getRoles();
        assertNotNull(returnedRoles);
        returnedRoles.forEach(returnedRole -> {
            assertEquals("USER", returnedRole.getName());
        });
        verify(this.userRepository).findByUserName("fred");
    }

}
