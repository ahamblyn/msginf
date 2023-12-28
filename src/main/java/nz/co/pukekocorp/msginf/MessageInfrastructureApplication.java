package nz.co.pukekocorp.msginf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Spring boot application
 */
@SpringBootApplication
public class MessageInfrastructureApplication {

    /**
     * Main method.
     * @param args arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(MessageInfrastructureApplication.class, args);
    }

}
