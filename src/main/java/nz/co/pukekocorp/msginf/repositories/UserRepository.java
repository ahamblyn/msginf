package nz.co.pukekocorp.msginf.repositories;

import nz.co.pukekocorp.msginf.entities.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * The user table repository.
 */
@Repository
public interface UserRepository extends CrudRepository<User, Integer> {

    /**
     * Find the user by name.
     * @param userName the user name.
     * @return the user found.
     */
    Optional<User> findByUserName(String userName);
}
