package de.minebugdevelopment.watch2minebug.controller;


import de.minebugdevelopment.watch2minebug.dtos.VideoDTO;
import de.minebugdevelopment.watch2minebug.entity.UserEntity;
import de.minebugdevelopment.watch2minebug.entity.VideoEntity;
import de.minebugdevelopment.watch2minebug.repository.UserRepository;
import de.minebugdevelopment.watch2minebug.repository.VideoRepository;
import de.minebugdevelopment.watch2minebug.security.SecurityUtils;
import de.minebugdevelopment.watch2minebug.utils.AWTUtil;
import de.minebugdevelopment.watch2minebug.utils.dto.DTObject;
import lombok.extern.slf4j.Slf4j;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.model.Picture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/videos")
public class VideoController {

    @Autowired private VideoRepository videoRepository;
    @Autowired private UserRepository userRepository;


    @GetMapping
    public List<DTObject> getVideos(@RequestParam(required = false, defaultValue = "false") String strShowAll) {
        List<VideoEntity> result;

        try {
            if (Boolean.parseBoolean(strShowAll)) {
                SecurityUtils.checkAuthority("video.list-all");
                result = videoRepository.findAll();
            } else {
                UserEntity user = userRepository.getById(SecurityUtils.getCurrentUser().getUserId());
                result = videoRepository.findAllByUploader(user);
            }
        } catch (SecurityException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return result.stream()
                .map(videoEntity -> new VideoDTO().parseFrom(videoEntity))
                .toList();
    }

    @PostMapping
    public ResponseEntity<?> loadVideo(@RequestParam String url) {
        VideoEntity videoEntity = new VideoEntity();
        videoEntity.setTitle("Test123");
        videoEntity.setContentPath("Breathedge_Advertisment_DE.mp4");
        videoRepository.save(videoEntity);
        return new ResponseEntity<>(HttpStatus.I_AM_A_TEAPOT);
    }


    @GetMapping(path = "stream/{uuid}", produces = "video/mp4")
    public Mono<Resource> getVideo(@PathVariable("uuid") String struuid, @RequestHeader(required = false, value = "Range") String range) throws IOException {

        Optional<VideoEntity> veo = videoRepository.findById(UUID.fromString(struuid));
        if (veo.isEmpty()) {
            log.debug("Video not found: {}", struuid);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        FileUrlResource fur = new FileUrlResource(new File("./videos/"+veo.get().getContentPath()).getAbsolutePath());
        return Mono.fromSupplier(() -> fur);
    }

    private static final float THUMB_ASPECT_RATIO = 16f/9f;
    private static final int THUMB_WIDTH = 256;
    private static final int THUMB_HEIGHT = (int) (THUMB_WIDTH/THUMB_ASPECT_RATIO);

    @GetMapping(path = "thumb/{uuid}")
    public ResponseEntity<byte[]> getThumb(@PathVariable("uuid") String struuid) {
        Optional<VideoEntity> veo = videoRepository.findById(UUID.fromString(struuid));
        if (veo.isEmpty()) {
            log.debug("Video not found: {}", struuid);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        try {
            Picture pic = FrameGrab.getFrameFromFile(new File("./videos/"+veo.get().getContentPath()), 0);
            Image image = AWTUtil.toBufferedImage(pic).getScaledInstance(THUMB_WIDTH, THUMB_HEIGHT, Image.SCALE_FAST);
            BufferedImage outputImage = new BufferedImage(THUMB_WIDTH, THUMB_HEIGHT, BufferedImage.TYPE_INT_RGB);
            outputImage.getGraphics().drawImage(image, 0, 0, null);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(outputImage, "png", outputStream);

            return ResponseEntity.status(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "filename=\"image.png\"")
                    .contentType(MediaType.IMAGE_PNG)
                    .body(outputStream.toByteArray());

        } catch (IOException | JCodecException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
