package nz.co.pukekocorp.msginf.repositories;

import nz.co.pukekocorp.msginf.models.user.User;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    public User findUserByUserName(String userName) {
        if (userName.equals("msginf")) {
            User user = new User(userName, "$2a$10$IMTTcjp2GBWjuJ9EbZ7zR.QZFEFPREBSM2RfjzBkonS3BNxP/sHUu", "Fred", "Dagg");
            return user;
        } else {
            return null;
        }
    }
}
