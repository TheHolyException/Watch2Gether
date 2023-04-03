package de.minebugdevelopment.watch2minebug.websocket.v3;

import de.minebugdevelopment.watch2minebug.Watch2Minebug;
import de.minebugdevelopment.watch2minebug.entity.UserEntity;
import de.minebugdevelopment.watch2minebug.repository.UserRepository;
import de.minebugdevelopment.watch2minebug.security.JwtTokenProvider;
import de.minebugdevelopment.watch2minebug.websocket.SocketManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.WebSocket;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Slf4j
public class WebSocketClient {

    private static Map<WebSocket, WebSocketClient> cachedInstances = Collections.synchronizedMap(new HashMap<>());

    @Getter
    private final WebSocket socket;

    @Getter
    private boolean isAuth = false;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Getter
    private String username = UUID.randomUUID().toString().substring(0,8);
    @Getter
    private UserEntity user;

    private WebSocketClient(WebSocket socket) {
        this.socket = socket;

        // Populating all @Autowired variables
        Watch2Minebug.getApplicationContext().getAutowireCapableBeanFactory().autowireBean(this);
    }

    public void onMessage(JSONObject message) {
        if (!isAuth) {
            if (authenticate(message)) {
                isAuth = true;
                socket.send("{\"auth\":\"true\"}");
            }
        } else {
            SocketManager.processChannel(message, this);
        }
    }

    public boolean authenticate(JSONObject data) {
        try {
            String token = (String) data.get("auth");
            if (jwtTokenProvider.validateToken(token)) {
                String username = jwtTokenProvider.getUsernameFromToken(token);
                Optional<UserEntity> user = userRepository.findByUsername(username);
                if (user.isPresent()) {
                    this.user = user.get();
                    this.username = this.user.getUsername();
                    return true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static WebSocketClient getClient(WebSocket socket) {
        return cachedInstances.computeIfAbsent(socket, WebSocketClient::new);
    }

}
