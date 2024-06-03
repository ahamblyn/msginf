/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.repositories;

import nz.co.pukekocorp.msginf.entities.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * The role table repository.
 */
@Repository
public interface RoleRepository extends CrudRepository<Role, Integer> {

    /**
     * Find the role by name.
     * @param name the role name.
     * @return the role found.
     */
    Optional<Role> findByName(String name);
}
