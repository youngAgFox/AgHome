package com.ag;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;

@ServerEndpoint(value = "/Server")
public class Server {

    private final IOException messageFormatException = 
        new IOException("Not a valid command. Messages should be in format: <cmd>[?argname=argvalue[;argname=argvalue]*]");

    public Server() {
        System.out.println("Testing server is being instantiated");
    }

    @OnOpen
    public void onOpen(Session session) throws IOException {
        // Get session and WebSocket connection
        System.out.println("Opened session: " + session);
    }

    @OnMessage
    public String onMessage(Session session, String message) throws IOException {
        System.out.println("Received message: " + session + " :: " + message);
        Message parsed = new Message(message);
        System.out.println(parsed);
        Map<String, String> params = parsed.getParameters();
        switch (parsed.getCommand()) {
            case "surrogateKey":
                if (!params.containsKey("keyName")) {
                    throw new IOException("Expected one argument <String: keyName>");
                }
                return String.valueOf(Synch.nextKey(params.get("keyName")));
            default: throw messageFormatException;
        }
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        // WebSocket connection closes
        System.out.println("Closed session: " + session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
        System.out.println("Received error: " + session + " :: " + throwable);
        throwable.printStackTrace();
    }

    private void broadcast(Session origin, String message) {
        for (Session session : origin.getOpenSessions()) {
            if (session.isOpen()) {
                session.getAsyncRemote().sendText(message);
            }
        }
    }

}
