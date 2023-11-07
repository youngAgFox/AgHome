package com.ag.Network;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

import com.ag.DynamicObject;
import com.ag.json.JsonConfig;
import com.ag.json.JsonFormatter;
import com.ag.json.JsonParser;

import java.io.IOException;

@ServerEndpoint(value = "/Server")
public class Server {

    public static final String CONFIGURATION_PATH = "/configs.json";
    public static final JsonConfig config = new JsonConfig(CONFIGURATION_PATH);
    public static final String ROOT_DIRECTORY = (String) config.get("rootDirectory");

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
    public String onMessage(Session session, String jsonData) throws IOException {
        JsonParser parser = new JsonParser();
        DynamicObject request = parser.parse(jsonData);
        System.out.println("Received message: " + session + " :: " + request.toString());

        ServerCommandHandler commandHandler = ServerCommandHandler.getInstance();

        DynamicObject response = commandHandler.handleRequest(request);

        if ((Boolean) response.get(ServerCommandHandler.ERROR_IND) 
                && commandHandler.isBroadcasted(request)) {
            broadcast(session, response);
        }
            
        writeRequestFields(response);
        JsonFormatter formatter = new JsonFormatter(true);
        return formatter.format(response);
    }

    private void writeRequestFields(DynamicObject args) {
        Long requestSeq = (Long) args.get(KEY_REQUEST_SEQUENCE);
        if (null == requestSeq) {
            args.put(ServerCommandHandler.ERROR_MESSAGE, args.getOrDefault(ServerCommandHandler.ERROR_MESSAGE, 
                    "") + "; ERROR: No request sequence");
            args.put(ServerCommandHandler.ERROR_IND, true);
        }
        args.put(ServerCommandHandler.COMMAND, COMMAND_RESPONSE);
        args.put(KEY_REQUEST_SEQUENCE, requestSeq);
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

    public void broadcast(Session origin, DynamicObject args) {
        JsonFormatter formatter = new JsonFormatter(true);
        String seralizedString = formatter.format(args);
        for (Session session : origin.getOpenSessions()) {
            if (origin.isOpen() && !origin.equals(session)) {
                session.getAsyncRemote().sendText(seralizedString);
            }
        }
    }

}
