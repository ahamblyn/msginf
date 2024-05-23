package nz.co.pukekocorp.msginf.repositories;

import nz.co.pukekocorp.msginf.models.user.User;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    public User findUserByUserName(String userName) {
        if (userName.equals("msginf")) {
            User user = new User(userName, "password123", "Fred", "Dagg");
            return user;

        } else {
            return null;
        }
    }
}
