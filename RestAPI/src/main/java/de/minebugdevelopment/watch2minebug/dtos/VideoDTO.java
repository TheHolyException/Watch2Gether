package de.minebugdevelopment.watch2minebug.dtos;

import de.minebugdevelopment.watch2minebug.utils.dto.DTOFieldResolver;
import de.minebugdevelopment.watch2minebug.utils.dto.DTObject;
import lombok.Getter;

import java.util.UUID;

@Getter
public class VideoDTO extends DTObject {

    @DTOFieldResolver("uuid")
    private UUID id;

    private String title;


}
