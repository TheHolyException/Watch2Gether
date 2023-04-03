package de.minebugdevelopment.watch2minebug.websocket.commands;

import de.minebugdevelopment.watch2minebug.Watch2Minebug;
import de.minebugdevelopment.watch2minebug.entity.CinemaEntity;
import de.minebugdevelopment.watch2minebug.entity.VideoEntity;
import de.minebugdevelopment.watch2minebug.repository.CinemaRepository;
import de.minebugdevelopment.watch2minebug.repository.VideoRepository;
import de.minebugdevelopment.watch2minebug.streamer.YoutubeStreamer;
import de.minebugdevelopment.watch2minebug.websocket.v3.SocketServer;
import de.minebugdevelopment.watch2minebug.websocket.v3.WebSocketClient;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class VideoControlChannel extends WSChannel {

    private final SocketServer socketServer = SocketServer.getInstance();
    private final Map<WebSocketClient, UUID> subscribedCinemas = Collections.synchronizedMap(new HashMap<>());

                    // CinemaID, VideoID
    private final Map<UUID, UUID> selectedVideo = Collections.synchronizedMap(new HashMap<>());

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private CinemaRepository cinemaRepository;

    public VideoControlChannel() {
        Watch2Minebug.getApplicationContext().getAutowireCapableBeanFactory().autowireBean(this);
    }

    @Override
    public boolean onReceive(JSONObject arguments, WebSocketClient client) throws IOException {

        System.out.println("Request: " + arguments.toJSONString());

        if (arguments.containsKey("subscribe")) {
            subscribedCinemas.remove(client);
            subscribedCinemas.put(client, UUID.fromString(arguments.get("subscribe").toString()));
            client.getSocket().send("""
                    {"channel":"videoctrl","subscribe":"ok"}
                    """);
            return true;
        }

        if (!subscribedCinemas.containsKey(client)) return false;
        UUID cinemaID = UUID.fromString(subscribedCinemas.get(client).toString());

        switch (arguments.get("command").toString()) {
            case "pause" -> {
                String response = getNewVidCtrlPacket("false", arguments.get("timestamp"));
                broadcastCinema(response, cinemaID);
            }

            case "play" -> {
                String response = getNewVidCtrlPacket("true", arguments.get("timestamp"));
                broadcastCinema(response, cinemaID);
            }

            case "seek" -> {
                String response = getNewVidCtrlPacket(arguments.get("playstate"), arguments.get("timestamp"));
                broadcastCinema(response, cinemaID);
            }

            case "select" -> {
                String response = String.format("""
                        {"channel":"videoctrl","command":"select","uuid":"%s"}
                        """,arguments.get("uuid"));
                selectedVideo.put(cinemaID, UUID.fromString(arguments.get("uuid").toString()));
                broadcastCinema(response, cinemaID);
            }

            case "sync" -> {
                // TODO
            }

            case "search" -> {
                String videoLink = (String) arguments.get("videolink");
                parseVideoSearch(videoLink,
                    complete -> {
                        System.out.println("Callback Test");
                        VideoEntity videoEntity = new VideoEntity();
                        videoEntity.setTitle(complete.getName().replace(".mp4", "").replace("_", " "));
                        videoEntity.setContentPath(complete.getName());
                        videoEntity.setUploader(client.getUser());

                        videoRepository.save(videoEntity);

                        Optional<CinemaEntity> oce = cinemaRepository.findById(cinemaID);
                        if (oce.isEmpty()) throw new IllegalStateException("MALAKA HOW THIS NOT POSSIBLE!!!! #6243");

                        CinemaEntity entity = oce.get();
                        Hibernate.initialize(entity.getVideolist());
                        entity.getVideolist().add(videoEntity);
                        cinemaRepository.save(entity);

                        broadcastCinema("""
                                {"channel":"videoctrl","command":"refreshvideolist"}
                                """, cinemaID);
                    },
                    progress -> {
                        String response = String.format("""
                            {"channel":"videoctrl","command":"progress","progress":"%s"}
                            """, progress);
                        broadcastCinema(response, cinemaID);
                    }
                );
            }

            default -> log.error("Invalid Command: " + arguments.get("command"));
        }

        return false;
    }

    @Override
    public void onClose(WebSocketClient client) {
        subscribedCinemas.remove(client);
    }

    private String getNewVidCtrlPacket(Object playstate, Object timestamp) {
        return String.format("""
                            {"channel":"videoctrl","command":"%s","playstate":"%s","timestamp":"%s"}
                            """, "vid", playstate, timestamp);
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

    public void broadcastCinema(String message, UUID cinemaID) {
        log.info("Sending: {}", message);
        //socketServer.broadcast(message);

        subscribedCinemas
                .entrySet()
                .stream()
                .filter(set -> set.getValue().equals(cinemaID))
                .map(Map.Entry::getKey)
                .forEach(client -> client.getSocket().send(message)
                );
    }

    public List<WebSocketClient> getClients(UUID cinemaID) {
        return subscribedCinemas
                .entrySet()
                .stream()
                .filter(set -> set.getValue().equals(cinemaID))
                .map(Map.Entry::getKey)
                .toList();
    }

    public UUID getSelectedVideo(UUID cinemaID) {
        return selectedVideo.get(cinemaID);
    }


}
