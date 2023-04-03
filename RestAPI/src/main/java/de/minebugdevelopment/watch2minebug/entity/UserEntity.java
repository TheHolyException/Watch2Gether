package de.minebugdevelopment.watch2minebug.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

import de.minebugdevelopment.watch2minebug.requests.UserChangeRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

//@Data // Data Anotation is eventually to complex for @CustomUserDetailsService #Bullshit
@Entity
@Table(name = "users")
@ToString(exclude = {"services", "usergroups"})
public class UserEntity implements IEntity {

    @Id
    @Getter
    @Type(type = "uuid-char")
    private UUID uuid = UUID.randomUUID();;

    @Column(unique = true, nullable = false)
    @Getter @Setter
    private String username;

    @JsonIgnore
    @Column(nullable = false)
    @Getter @Setter
    private String password;

    @Getter @Setter
    private String firstName;

    @Getter @Setter
    private String lastName;

    @JoinTable(
            name = "_MTM_user_usergroups",
            joinColumns = {
                    @JoinColumn(name = "userlink")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "usergrouplink")
            }

    )
    @ElementCollection(targetClass=UserGroupEntity.class)
    private Set<UserGroupEntity> usergroups = new HashSet<>();

    public UserEntity(UserChangeRequest userChangeRequest) {
        this.username = userChangeRequest.getUsername();
        this.password = userChangeRequest.getPassword();
        this.firstName = userChangeRequest.getFirstName();
        this.lastName = userChangeRequest.getLastName();
    }


    /**
     * @return Returns all user groups of this user (also containing deep group references)
     */
    public Set<UserGroupEntity> getUserGroups() {
        if (usergroups.isEmpty()) return new HashSet<>(0);
        return deepScanGroups((UserGroupEntity) usergroups.toArray()[0], new HashSet<>());
    }

    /**
     * If the group has a parent, add the parent to the result set and recursively call the function on the parent.
     *
     * @param toScan The group to scan
     * @param result The set of groups that will be returned.
     * @return A set of all the groups that the user is a member of.
     */
    private Set<UserGroupEntity> deepScanGroups(UserGroupEntity toScan, Set<UserGroupEntity> result) {
        result.add(toScan);
        UserGroupEntity parent;
        if (( parent = toScan.getParent()) != null) deepScanGroups(parent, result);
        return result;
    }

    public void setUsergroups(Set<UserGroupEntity> groups) {
        this.usergroups = groups;
    }

    @Deprecated(forRemoval = true)
    public boolean hasPermissions(String permission) {
        boolean result = false;
        for (UserGroupEntity uge : getUserGroups()) {
            for (String perm : uge.getPermissions()) {
                if (perm.equals(permission) || perm.equals("*")) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public UserEntity() {}

}
