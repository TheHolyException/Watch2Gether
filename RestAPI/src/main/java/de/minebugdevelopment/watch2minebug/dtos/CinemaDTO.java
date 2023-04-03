package de.minebugdevelopment.watch2minebug.dtos;

import de.minebugdevelopment.watch2minebug.entity.VideoEntity;
import de.minebugdevelopment.watch2minebug.utils.dto.DTOFieldResolver;
import de.minebugdevelopment.watch2minebug.utils.dto.DTOMethodResolver;
import de.minebugdevelopment.watch2minebug.utils.dto.DTObject;
import lombok.Getter;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
public class CinemaDTO extends DTObject {

    @DTOFieldResolver("uuid")
    private UUID id;

    @DTOFieldResolver({"owner", "uuid"})
    private UUID ownerID;

    @DTOMethodResolver(methode = "parseVideoLinks", source = "videolist")
    private List<UUID> videolinks;

    private String name;

    @SuppressWarnings("unchecked")
    private Object parseVideoLinks(Object obj) {
        return ((Set<VideoEntity>) obj).stream().map(VideoEntity::getUuid).toList();
    }

}
