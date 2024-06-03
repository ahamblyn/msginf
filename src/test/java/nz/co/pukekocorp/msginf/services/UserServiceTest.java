/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.services;

import nz.co.pukekocorp.msginf.entities.Role;
import nz.co.pukekocorp.msginf.entities.User;
import nz.co.pukekocorp.msginf.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        Optional<User> returnedUserOpt = this.userService.getUserByUserName("fred");

        // Then
        assertTrue(returnedUserOpt.isPresent());
        returnedUserOpt.ifPresent(returnedUser -> {
            assertEquals("fred", returnedUser.getUsername());
            assertEquals("qwerty123", returnedUser.getPassword());
            assertEquals("Fred", returnedUser.getFirstName());
            assertEquals("Dagg", returnedUser.getLastName());
            Set<Role> roles = returnedUser.getRoles();
            assertNotNull(roles);
            roles.forEach(role -> {
                assertEquals("USER", role.getName());
                assertEquals("User role", role.getDescription());
            });
        });
        verify(this.userRepository).findByUserName("fred");
    }

}
