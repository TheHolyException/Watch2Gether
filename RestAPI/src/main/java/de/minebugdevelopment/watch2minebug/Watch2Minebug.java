package de.minebugdevelopment.watch2minebug;

import de.minebugdevelopment.watch2minebug.streamer.YoutubeStreamer;
import de.minebugdevelopment.watch2minebug.utils.ComponentRegistry;
import de.minebugdevelopment.watch2minebug.websocket.SocketManager;
import de.minebugdevelopment.watch2minebug.websocket.commands.ChatChannel;
import de.minebugdevelopment.watch2minebug.websocket.commands.ServiceChannel;
import de.minebugdevelopment.watch2minebug.websocket.commands.VideoControlChannel;
import de.minebugdevelopment.watch2minebug.websocket.v3.SocketServer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@SpringBootApplication
public class Watch2Minebug {

    @Getter
    private static ConfigurableApplicationContext applicationContext;

    public static void main(String[] args) throws Exception {
        applicationContext = SpringApplication.run(Watch2Minebug.class, "");
        ComponentRegistry.init();

        try {
            // Initializing WebSocket connection
            SocketServer.getInstance();
            SocketManager.registerChannel("service", new ServiceChannel());
            SocketManager.registerChannel("videoctrl", new VideoControlChannel());
            SocketManager.registerChannel("chat", new ChatChannel());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

/*         YoutubeStreamer.getInstance().downloadVideo("qlfVVymbzi4",
                complete -> {
                    System.out.println("Got file: " + complete.getAbsolutePath());
                },
                progress -> {
                    System.out.println("Downloading: " + progress);
                }
        );*/

    }

}
