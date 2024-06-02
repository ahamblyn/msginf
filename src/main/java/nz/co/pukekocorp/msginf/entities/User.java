package nz.co.pukekocorp.msginf.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

/**
 * User database entity.
 */
@Table(name = "user")
@Entity
@Getter
@Setter
public class User implements UserDetails {

    /**
     * The primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Integer id;

    /**
     * The user name.
     */
    @Column(unique = true, length = 100, nullable = false)
    private String userName;

    /**
     * The user's password.
     */
    @Column(nullable = false, length = 60)
    private String password;

    /**
     * The user's first name.
     */
    @Column()
    private String firstName;

    /**
     * The user's last name.
     */
    @Column()
    private String lastName;

    /**
     * The entity creation time.
     */
    @CreationTimestamp
    @Column(updatable = false, name="created_at")
    private Date createdAt;

    /**
     * The entity update time.
     */
    @UpdateTimestamp
    @Column(name="updated_at")
    private Date updatedAt;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(name = "user_role",
            joinColumns = {
                    @JoinColumn(name = "user_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "role_id") })
    private Set<Role> roles;

    /**
     * Create a user.
     */
    public User() {}

    /**
     * Create a user.
     * @param userName the user name.
     * @param password the user's password.
     * @param firstName the user's first name.
     * @param lastName the user's last name.
     */
    public User(String userName, String password, String firstName, String lastName) {
        this.userName = userName;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * The user's granted authorities.
     * @return the user's granted authorities.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        this.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
        });
        return authorities;
    }

    /**
     * Gets the user name.
     * @return the user name.
     */
    @Override
    public String getUsername() {
        return userName;
    }

    /**
     * Whether the account is non-expired.
     * @return whether the account is non-expired.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Whether the account is non-locked.
     * @return whether the account is non-locked.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Whether the credentials are non-expired.
     * @return whether the credentials are non-expired.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Whether the user is enabled.
     * @return whether the user is enabled.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
