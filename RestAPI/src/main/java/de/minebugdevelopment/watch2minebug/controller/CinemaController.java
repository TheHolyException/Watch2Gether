package de.minebugdevelopment.watch2minebug.controller;

import de.minebugdevelopment.watch2minebug.dtos.CinemaDTO;
import de.minebugdevelopment.watch2minebug.dtos.VideoDTO;
import de.minebugdevelopment.watch2minebug.entity.CinemaEntity;
import de.minebugdevelopment.watch2minebug.entity.UserEntity;
import de.minebugdevelopment.watch2minebug.entity.VideoEntity;
import de.minebugdevelopment.watch2minebug.repository.CinemaRepository;
import de.minebugdevelopment.watch2minebug.repository.UserRepository;
import de.minebugdevelopment.watch2minebug.repository.VideoRepository;
import de.minebugdevelopment.watch2minebug.requests.URLRequest;
import de.minebugdevelopment.watch2minebug.security.SecurityUtils;
import de.minebugdevelopment.watch2minebug.streamer.YoutubeStreamer;
import de.minebugdevelopment.watch2minebug.utils.dto.DTObject;
import de.minebugdevelopment.watch2minebug.websocket.SocketManager;
import de.minebugdevelopment.watch2minebug.websocket.commands.VideoControlChannel;
import de.minebugdevelopment.watch2minebug.websocket.commands.WSChannel;
import de.minebugdevelopment.watch2minebug.websocket.v3.WebSocketClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@RestController
@RequestMapping("/cinema")
public class CinemaController {

    @Autowired private CinemaRepository cinemaRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private VideoRepository videoRepository;

    @GetMapping
    public List<DTObject> getCinemas() {
        return cinemaRepository.findAll().stream().map(x -> new CinemaDTO().parseFrom(x)).toList();
    }

    @GetMapping("/{uuid}")
    public DTObject getCinemaData(@PathVariable("uuid") String struuid) {

        // TODO Check permissions for this cinema

        Optional<CinemaEntity> oce = cinemaRepository.findById(UUID.fromString(struuid));
        if (oce.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return new CinemaDTO().parseFrom(oce.get());
    }

    @GetMapping("/create")
    public DTObject createCinema() {
        try {
            SecurityUtils.checkAuthority("cinema.create");
            UserEntity user = userRepository.getById(SecurityUtils.getCurrentUser().getUserId());

            CinemaEntity cinema = new CinemaEntity();
            cinema.setOwner(user);

            cinemaRepository.save(cinema);
            return new CinemaDTO().parseFrom(cinema);
        } catch (SecurityException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/{uuid}/clients")
    public List<String> getClients(@PathVariable("uuid") String struuid) {
        Optional<WSChannel> channelOptional = SocketManager.getChannel("videoctrl");
        if (channelOptional.isEmpty()) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);

        VideoControlChannel vcc = (VideoControlChannel) channelOptional.get();
        return vcc.getClients(UUID.fromString(struuid)).stream().map(WebSocketClient::getUsername).toList();
    }

    @GetMapping("/{uuid}/videos")
    public List<DTObject> getVideos(@PathVariable("uuid") String struuid) {
        Optional<CinemaEntity> oce = cinemaRepository.findById(UUID.fromString(struuid));
        if (oce.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return oce.get().getVideolist().stream().map(videoEntity -> new VideoDTO().parseFrom(videoEntity)).toList();
    }

    @GetMapping("/{uuid}/currentvideo")
    public DTObject getVideo(@PathVariable("uuid") String struuid) {
        Optional<VideoControlChannel> ovc = SocketManager.getChannel(VideoControlChannel.class);
        if (ovc.isEmpty()) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);

        UUID videoID = ovc.get().getSelectedVideo(UUID.fromString(struuid));
        if (videoID == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        Optional<VideoEntity> ove = videoRepository.findById(videoID);
        if (ove.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return new VideoDTO().parseFrom(ove.get());
    }


    @PostMapping("/{uuid}/search")
    public void searchVideo(@PathVariable("uuid") String struuid, @RequestBody URLRequest request) {
        VideoControlChannel vcc = SocketManager.getChannel(VideoControlChannel.class).get();
        UUID cinemaID = UUID.fromString(struuid);
        UserEntity user = null;
        try {
            userRepository.findById(SecurityUtils.getCurrentUser().getUserId());
        } catch (SecurityException ex) {
            // ignore
        }

        System.out.println(request.getUrl());

        parseVideoSearch(request.getUrl(),
                complete -> {
                    System.out.println("Callback Test");
                    VideoEntity videoEntity = new VideoEntity();
                    videoEntity.setTitle(complete.getName().replace(".mp4", "").replace("_", " "));
                    videoEntity.setContentPath(complete.getName());
                    videoEntity.setUploader(user);

                    videoRepository.save(videoEntity);

                    Optional<CinemaEntity> oce = cinemaRepository.findById(cinemaID);
                    if (oce.isEmpty()) throw new IllegalStateException("MALAKA HOW THIS NOT POSSIBLE!!!! #6243");

                    CinemaEntity entity = oce.get();
                    entity.getVideolist().add(videoEntity);
                    cinemaRepository.save(entity);

                    vcc.broadcastCinema("""
                                {"channel":"videoctrl","command":"refreshvideolist"}
                                """, cinemaID);
                },
                progress -> {
                    String response = String.format("""
                            {"channel":"videoctrl","command":"progress","progress":"%s"}
                            """, progress);
                    vcc.broadcastCinema(response, cinemaID);
                }
        );
    }

    private void parseVideoSearch(String videoLink, Consumer<File> completeCallback, Consumer<Integer> progressCallback) {
        try {
            URL url = URI.create(videoLink).toURL();

            switch (url.getHost()) {
                case "www.youtube.com", "www.youtu.be" -> {
                    YoutubeStreamer.getInstance().downloadVideo(url.getQuery().substring(2), completeCallback, progressCallback);
                }
            }
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }

    }

}
