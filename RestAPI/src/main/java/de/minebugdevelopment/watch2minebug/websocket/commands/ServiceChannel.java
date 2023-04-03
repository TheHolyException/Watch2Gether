package de.minebugdevelopment.watch2minebug.websocket.commands;

import de.minebugdevelopment.watch2minebug.websocket.v3.WebSocketClient;
import org.json.simple.JSONObject;

import java.io.IOException;

public class ServiceChannel extends WSChannel {

    @Override
    public boolean onReceive(JSONObject arguments, WebSocketClient client) throws IOException {
        client.getSocket().send("[\"Response From ServiceChannel\"]");
        return false;
    }

}
