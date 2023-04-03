package de.minebugdevelopment.watch2minebug.requests;

import de.minebugdevelopment.watch2minebug.entity.UserEntity;
import lombok.Data;

@Data
public class UserChangeRequest {

    private String username;
    private String firstName;
    private String lastName;
    private String password;

    public void appendChangesTo(UserEntity entity) {
        entity.setUsername(this.username);
        entity.setFirstName(this.firstName);
        entity.setLastName(this.lastName);
        entity.setPassword(this.password);
    }

}
