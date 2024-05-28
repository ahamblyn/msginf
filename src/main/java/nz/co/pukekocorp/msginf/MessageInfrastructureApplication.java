package nz.co.pukekocorp.msginf;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukekocorp.msginf.entities.User;
import nz.co.pukekocorp.msginf.repositories.UserRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

/**
 * Spring boot application
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class})
@Slf4j
public class MessageInfrastructureApplication {

    @Value("${msginf.database.autoload-user}")
    private boolean autoloadUser;

    @Autowired
    private UserRepository userRepository;

    /**
     * Main method.
     * @param args arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(MessageInfrastructureApplication.class, args);
    }

    @Bean
    InitializingBean initializeUser() {
        return () -> {
            if (autoloadUser) {
                log.info("Autoload user.");
                Optional<User> userOpt = userRepository.findByUserName("msginf");
                userOpt.ifPresentOrElse(user -> log.info(user.getUsername() + " user already exists"), () -> {
                    log.info("Creating msginf user");
                    User user = new User("msginf", "$2a$10$IMTTcjp2GBWjuJ9EbZ7zR.QZFEFPREBSM2RfjzBkonS3BNxP/sHUu",
                            "Fred", "Dagg");
                    userRepository.save(user);
                });
            } else {
                log.info("User not autoloaded.");
            }
        };
    }

}
