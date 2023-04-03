package de.minebugdevelopment.watch2minebug.websocket.v3;

import de.minebugdevelopment.watch2minebug.Watch2Minebug;
import de.minebugdevelopment.watch2minebug.websocket.SocketManager;
import de.minebugdevelopment.watch2minebug.websocket.commands.VideoControlChannel;
import de.minebugdevelopment.watch2minebug.websocket.commands.WSChannel;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class SocketServer extends WebSocketServer {

    private static SocketServer instance;

    public static SocketServer getInstance() {
        if (instance != null) return instance;
        Environment env = Watch2Minebug.getApplicationContext().getEnvironment();
        String host = env.getProperty("websocket.host");
        Integer port = Integer.parseInt(Objects.requireNonNull(env.getProperty("websocket.port")));
        instance = new SocketServer(host, port);
        return instance;
    }

    @Value("${websocket.ssl.storepassword}")
    private String storepassword;

    @Value("${websocket.ssl.storetype}")
    private String storetype;

    private SocketServer(String host, Integer port) {
        super(new InetSocketAddress(host, port));
        Watch2Minebug.getApplicationContext().getAutowireCapableBeanFactory().autowireBean(this);

        try {
            //String STOREPASSWORD = "y2f#$TK@#%g!24*%4$ckGiFFrLR2jd@";
            String KEYPASSWORD = "nVvkr8mW^3w^Yf$5!jP*tWS%Gvb6g$%";

            KeyStore ks = KeyStore.getInstance(storetype);
            File kf = new File("./cert/websocket.jks");
            ks.load(new FileInputStream(kf), storepassword.toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, storepassword.toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        start();
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        System.out.println(webSocket.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!");
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        Optional<VideoControlChannel> channel = SocketManager.getChannel(VideoControlChannel.class);
        if (channel.isEmpty()) throw new IllegalStateException("SocketChannel not found: videoctrl");
        channel.get().onClose(WebSocketClient.getClient(webSocket));
    }

    @Override
    public void onMessage(WebSocket socket, String s) {
        try {
            JSONObject data = (JSONObject) new JSONParser().parse(s);
            if (data == null) {
                System.out.println("RAW:" + s);
            }
            WebSocketClient.getClient(socket).onMessage(data);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void onError(WebSocket webSocket, Exception ex) {
        ex.printStackTrace();
        if (webSocket != null) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    @Override
    public void onStart() {
        log.info("Started WebSocket server on: {} port: {}", this.getAddress().getHostName(), this.getPort());
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }
}
