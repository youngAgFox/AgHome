package com.ag;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    public String onMessage(Session session, String messageData) throws IOException {
        System.out.println("Received message: " + session + " :: " + messageData);
        Message received = Message.parseMessage(messageData);
        System.out.println(received);
        Map<String, String> params = received.getParameters();
        Map<String, String> args = new HashMap<>();

        switch (received.getCommand()) {
            case "surrogateKey":
                if (!params.containsKey("key")) {
                    return createErrorMessage("Expected one argument <String: key>").toMessageString();
                }
                args.put("value", String.valueOf(Synch.nextKey(params.get("key"))));
                return createResponseMessage(received, args).toMessageString();
            case "createStore":
                if (!params.containsKey("name")) {
                    return createErrorMessage("Expected one argument <String: name>").toMessageString();
                }
            default: return createErrorMessage("Unknown command: '" + received.getCommand() + "'").toMessageString();
        }
    }

    private Message createResponseMessage(Message received, Map<String, String> args) {
        String requestSeq = received.getParameters().get("requestSeq");
        if (null == requestSeq) {
            return createErrorMessage("Missing 'requestSeq'!");
        } 
        Message message = new Message("response", args);
        message.getParameters().put("requestSeq", requestSeq);
        return message;
    }

    private Message createErrorMessage(String errorMessage) {
        Message message = new Message("error");
        message.getParameters().put("value", errorMessage);
        return message;
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

    private void broadcast(Session origin, Message message) {
        for (Session session : origin.getOpenSessions()) {
            if (session.isOpen()) {
                session.getAsyncRemote().sendText(message.toMessageString());
            }
        }
    }

}
