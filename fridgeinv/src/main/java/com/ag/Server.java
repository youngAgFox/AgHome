package com.ag;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

import com.ag.database.FlatFileStorer;
import com.ag.database.Storer;
import com.ag.database.InventoryItem;
import com.ag.database.Store;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ServerEndpoint(value = "/Server")
public class Server {

    private FlatFileStorer<InventoryItem> itemStorer = new FlatFileStorer<>();
    private FlatFileStorer<Store> storeStorer = new FlatFileStorer<>();

    public Server() {
        System.out.println("Server instantiation start");
        try {
            itemStorer.open("itemStorer_meta_version1", "itemStorer_version1");
            storeStorer.open("storeStorer_meta_version1", "storeStorer_version1");


            // TESTING
            InventoryItem item = new InventoryItem(Synch.getInstance().nextKey("INV_ITEM"));
            itemStorer.save(item);
            // InventoryItem item = itemStorer.load(0);
            // System.out.println("Item: " + item.getId());
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize storer objects: ", e);
        }

        System.out.println("Server instantiated successfully");
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
                args.put("value", String.valueOf(Synch.getInstance().nextKey(params.get("key"))));
                return createResponseMessage(received, args).toMessageString();
            case "createStore":
                if (!params.containsKey("name")) {
                    return createErrorMessage("Expected one argument <String: name>").toMessageString();
                }
                args.put("value", String.valueOf("2"));
                return createResponseMessage(received, args).toMessageString();
            case "createItem":

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
