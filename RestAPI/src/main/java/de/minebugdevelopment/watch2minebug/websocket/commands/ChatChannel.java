package de.minebugdevelopment.watch2minebug.websocket.commands;

import de.minebugdevelopment.watch2minebug.websocket.v3.WebSocketClient;
import org.json.simple.JSONObject;

import java.io.IOException;

public class ChatChannel extends WSChannel {

    @Override
    public boolean onReceive(JSONObject arguments, WebSocketClient client) throws IOException {
        return false;
    }

}
