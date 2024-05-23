package nz.co.pukekocorp.msginf.services;

import nz.co.pukekocorp.msginf.entities.User;
import nz.co.pukekocorp.msginf.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JWT User details service.
 */
@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        Optional<User> userOpt = userRepository.findByUserName(userName);
        User user = userOpt.orElseThrow(() -> new UsernameNotFoundException("User " + userName + " not found."));
        List<String> roles = new ArrayList<>();
        roles.add("USER");
        return user;
    }
}
