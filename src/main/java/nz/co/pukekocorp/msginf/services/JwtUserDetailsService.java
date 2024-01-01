package nz.co.pukekocorp.msginf.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * User details service.
 */
@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Value("${jwt.secret}")
    private String secret;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (secret.equals(username)) {
            return new User(secret, "$2a$10$IMTTcjp2GBWjuJ9EbZ7zR.QZFEFPREBSM2RfjzBkonS3BNxP/sHUu", new ArrayList<>());
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }
}
