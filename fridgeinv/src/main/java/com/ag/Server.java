package com.ag;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ServerEndpoint(value = "/Server")
public class Server {


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
        // Handle new messages
        System.out.println("Received message: " + session + " :: " + message);
        return "HELLO from the server!";
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
    }
}
