package com.ag;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ag.database.FlatFileStorer;
import com.ag.database.InventoryItem;
import com.ag.database.Store;

public class ServerCommandHandler {

    // commands
    private static final String CREATE_INVENTORY_ITEM = "create_inv_item";
    private static final String CREATE_STORE = "create_store";
    private static final String GET_NEXT_SURROGATE_KEY = "get_next_skey";
    private static final String GET_SURROGATE_KEY = "get_current_skey";

    // argument keys
    public static final String RETURN_VALUE = "value";
    public static final String ERROR_IND = "error_ind";
    public static final String ERROR_MESSAGE = "error_msg";

    // storer directories
    private static final String STORER_DIR = ".storage" + File.pathSeparator;
    private static final String ITEM_STORER_PATH = STORER_DIR + "item_storer";
    private static final String ITEM_STORER_META_PATH = ITEM_STORER_PATH + "_meta";
    private static final String STORE_STORER_PATH = STORER_DIR + "store_storer";
    private static final String STORE_STORER_META_PATH = STORE_STORER_PATH + "_meta";

    private static ServerCommandHandler instance;

    private Map<String, CommandHandler> commandHandlers = new HashMap<>();
    private Set<String> broadcastCommands = new HashSet<>();
    private FlatFileStorer<InventoryItem> itemStorer = new FlatFileStorer<>();
    private FlatFileStorer<Store> storeStorer = new FlatFileStorer<>();

    private ServerCommandHandler() {
        // handlers setup
        commandHandlers.put(CREATE_INVENTORY_ITEM, params -> createInventoryItem(params));
        commandHandlers.put(CREATE_STORE, params -> createStore(params));
        commandHandlers.put(GET_SURROGATE_KEY, params -> getSurrogateKey(params));
        commandHandlers.put(GET_NEXT_SURROGATE_KEY, params -> getNextSurrogateKey(params));

        // broadcast commands setup
        broadcastCommands.add(CREATE_STORE);
        broadcastCommands.add(CREATE_INVENTORY_ITEM);

        try {
            // storer setup
            itemStorer.open(ITEM_STORER_META_PATH, ITEM_STORER_PATH);
            storeStorer.open(STORE_STORER_META_PATH, STORE_STORER_PATH);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize storer objects: ", e);
        }
    }

    public static ServerCommandHandler getInstance() {
        if (null == instance) {
            instance = new ServerCommandHandler();
        }
        return instance;
    }

    private interface CommandHandler {
        Map<String, String> handle(Map<String, String> params);
    }

    public Map<String, String> handleCommand(String command, Map<String, String> parameters) {
        return commandHandlers.getOrDefault(command, params -> returnNoSuchCommand(command, params)).handle(parameters);
    }

    public boolean isBroadcasted(String command) {
        return broadcastCommands.contains(command);
    }
    
    public Map<String, String> createInventoryItem(Map<String, String> params) {
        Map<String, String> args = initResponseArgs();
        // FIXME
        return returnError(args, "Unimplemented");
    }

    public Map<String, String> createStore(Map<String, String> params) {
        Map<String, String> args = initResponseArgs();
        List<String> missingReqParams = getMissingRequiredParams(params, "name");
        if (!missingReqParams.isEmpty()) {
            returnError(args, getMissingRequiredParamsErrorMessage(missingReqParams));
        }
        try {
            if (storeStorer.contains(store -> params.get("name").equalsIgnoreCase(store.getName()))) {
                return returnError(args, "Store with the name '" + params.get("name") + "' already exists.");
            }
        } catch (IOException e) {
            return returnError(args, "Failed to search database for the store name");
        }
        try {
            Store store = new Store(Synch.getInstance().nextKey(Synch.STORE_SURROGATE_KEY), params.get("name"));
            storeStorer.save(store);
        } catch (IOException e) {
            return returnError(args, "Failed to save new store");
        }
        return returnBoolean(args, true);
    }

    public Map<String, String> getSurrogateKey(Map<String, String> params) {
        Map<String, String> args = initResponseArgs();
        // FIXME
        return returnError(args, "Unimplemented");
    }

    public Map<String, String> getNextSurrogateKey(Map<String, String> params) {
        Map<String, String> args = initResponseArgs();
        List<String> missingReqParams = getMissingRequiredParams(params, "key");
        if (!missingReqParams.isEmpty()) {
            returnError(args, getMissingRequiredParamsErrorMessage(missingReqParams));
        }
        return returnLong(args, Synch.getInstance().nextKey(params.get("key")));
    }

    private List<String> getMissingRequiredParams(Map<String, String> params, String ... requiredParams) {
        List<String> missingRequiredParams = new LinkedList<>();
        for (String reqParam : requiredParams) {
            if (!params.containsKey(reqParam)) {
                missingRequiredParams.add(reqParam);
            }
        }
        return missingRequiredParams;
    }

    private String getMissingRequiredParamsErrorMessage(List<String> missingReqParams) {
        if (missingReqParams.isEmpty()) {
            return "There are zero missing required parameters.";
        }
        StringBuilder sb = new StringBuilder("Missing: ");
        for (String reqParam : missingReqParams) {
            sb.append("<").append(reqParam).append(">, ");
        }
        // truncate the ', ' at the end of the built String
        sb.setLength(sb.length() - 2);
        return sb.toString();
    }

    private Map<String, String> returnDouble(Map<String, String> args, double value) {
        args.put(RETURN_VALUE, String.valueOf(value));
        return args;
    }

    private Map<String, String> returnString(Map<String, String> args, String value) {
        args.put(RETURN_VALUE, value);
        return args;
    }

    private Map<String, String> returnInt(Map<String, String> args, int value) {
        args.put(RETURN_VALUE, String.valueOf(value));
        return args;
    }

    private Map<String, String> returnLong(Map<String, String> args, long value) {
        args.put(RETURN_VALUE, String.valueOf(value));
        return args;
    }

    private Map<String, String> returnBoolean(Map<String, String> args, boolean value) {
        args.put(RETURN_VALUE, String.valueOf(value));
        return args;
    }

    private Map<String, String> returnNoSuchCommand(String command, Map<String, String> params) {
        return returnError(params, "Unknown command: '" + command + "'");
    }

    private Map<String, String> returnError(Map<String, String> args, String message) {
        args.put(ERROR_IND, String.valueOf(true));
        args.put(ERROR_MESSAGE, message);
        return args;
    }

    private Map<String, String> initResponseArgs() {
        Map<String, String> args = new HashMap<>();
        args.put(ERROR_IND, String.valueOf(false));
        return args;
    }
}
