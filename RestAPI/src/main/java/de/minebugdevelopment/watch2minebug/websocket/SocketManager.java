package de.minebugdevelopment.watch2minebug.websocket;

import de.minebugdevelopment.watch2minebug.websocket.commands.WSChannel;
import de.minebugdevelopment.watch2minebug.websocket.v3.WebSocketClient;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

import java.util.*;

@Slf4j
public class SocketManager {

    private SocketManager() {}

    private static final Map<String, WSChannel> commandMap = new HashMap<>();
    public static void registerChannel(String channel, WSChannel channelHandler) {
        commandMap.put(channel.toLowerCase(Locale.ROOT), channelHandler);
    }
    public static void processChannel(JSONObject input, WebSocketClient client) {
        try {
            if (input == null || input.get("channel") == null) return;
            WSChannel channel = commandMap.get(input.get("channel").toString().toLowerCase(Locale.ROOT));

            if (channel != null) {
                channel.onReceive(input, client);
                client.getSocket().send("""
                        {"result":"200"}
                        """); // OK
            } else {
                log.warn("Channel {} not found!", input.get("channel"));
                client.getSocket().send(""" 
                    {"result":"401"}
                    """); // Channel not found
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Optional<WSChannel> getChannel(String identifier) {
        if (commandMap.containsKey(identifier)) return Optional.of(commandMap.get(identifier));
        else return Optional.empty();
    }

    public static <T extends WSChannel> Optional<T> getChannel(Class<T> channelclass) {
        return (Optional<T>) commandMap.values().stream().filter(channel -> channel.getClass().equals(channelclass)).findFirst();
    }
}
