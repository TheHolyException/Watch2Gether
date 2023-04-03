package de.minebugdevelopment.watch2minebug.dtos;

import de.minebugdevelopment.watch2minebug.utils.dto.DTOFieldResolver;
import de.minebugdevelopment.watch2minebug.utils.dto.DTObject;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserDTO extends DTObject {

    @DTOFieldResolver("uuid")
    private UUID id;

    private String username;

    private String firstName;

    private String lastName;

}
