package com.ag;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Map;

@ServerEndpoint(value = "/Server")
public class Server {

    private static final String KEY_REQUEST_SEQUENCE = "request_seq";
    private static final String COMMAND_RESPONSE = "response";

    public Server() {
        System.out.println("Server instantiated");
    }

    @OnOpen
    public void onOpen(Session session) throws IOException {
        System.out.println("Opened session: " + session);
    }

    @OnMessage
    public String onMessage(Session session, String messageData) throws IOException {
        System.out.println("Received message: " + session + " :: " + messageData);
        Message received = Message.parseMessage(messageData);
        System.out.println(received);

        ServerCommandHandler commandHandler = ServerCommandHandler.getInstance();

        Map<String, String> responseArgs = commandHandler.handleCommand(received.getCommand(), received.getParameters());
        Message response = createResponseMessage(received, responseArgs);

        if (Boolean.valueOf(responseArgs.get(ServerCommandHandler.ERROR_IND)) 
                && commandHandler.isBroadcasted(received.getCommand())) {

            Message broadcastMessage = new Message(received.getCommand(), response.getParameters());
            broadcast(session, broadcastMessage);
        }
            
        return response.toSerializedString();
    }

    private Message createResponseMessage(Message received, Map<String, String> args) {
        String requestSeq = received.getParameters().get(KEY_REQUEST_SEQUENCE);
        if (null == requestSeq) {
            args.put(ServerCommandHandler.ERROR_MESSAGE, args.getOrDefault(ServerCommandHandler.ERROR_MESSAGE, 
                    "") + "; ERROR: No request sequence");
            args.put(ServerCommandHandler.ERROR_IND, String.valueOf(true));
        }
        Message message = new Message(COMMAND_RESPONSE, args);
        message.getParameters().put(KEY_REQUEST_SEQUENCE, requestSeq);
        return message;
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        System.out.println("Closed session: " + session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("Received error: " + session + " :: " + throwable);
        throwable.printStackTrace();
    }

    public void broadcast(Session origin, Message message) {
        for (Session session : origin.getOpenSessions()) {
            if (origin.isOpen() && !origin.equals(session)) {
                session.getAsyncRemote().sendText(message.toSerializedString());
            }
        }
    }

}
