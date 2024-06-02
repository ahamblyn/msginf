/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Role database entity.
 */
@Table(name = "role")
@Entity()
@Getter
@Setter
public class Role {

    /**
     * The primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private long id;

    /**
     * The role name.
     */
    @Column(nullable = false)
    private String name;

    /**
     * The role description.
     */
    @Column
    private String description;

    /**
     * Create a role.
     */
    public Role() {}

    /**
     * Create a role.
     * @param name the role name.
     * @param description the role description.
     */
    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
