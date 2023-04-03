package de.minebugdevelopment.watch2minebug.websocket.commands;

import de.minebugdevelopment.watch2minebug.websocket.v3.WebSocketClient;
import org.json.simple.JSONObject;

import java.io.IOException;

public abstract class WSChannel {

    public abstract boolean onReceive(JSONObject arguments, WebSocketClient client) throws IOException;

    public void onClose(WebSocketClient client) {}
}
