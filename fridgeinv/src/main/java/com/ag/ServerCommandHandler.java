package com.ag;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ag.database.InventoryItem;
import com.ag.database.Storable;
import com.ag.database.Store;
import com.ag.database.Storer;
import com.ag.database.StorerFactory;

public class ServerCommandHandler {

    // commands
    private static final String CREATE_INVENTORY_ITEM = "create_inv_item";
    private static final String CREATE_STORE = "create_store";
    private static final String GET_ALL_STORE = "get_all_store";
    private static final String GET_ALL_SHELF = "get_all_shelf";
    private static final String GET_ALL_SHELF_INV_ITEM = "get_all_shelf_inv_item";

    // argument keys
    public static final String RETURN_VALUE = "value";
    public static final String ERROR_IND = "error_ind";
    public static final String ERROR_MESSAGE = "error_msg";

    private static ServerCommandHandler instance;

    private Map<String, CommandHandler> commandHandlers = new HashMap<>();
    private Set<String> broadcastCommands = new HashSet<>();

    private ServerCommandHandler() {
        // handlers setup
        commandHandlers.put(CREATE_INVENTORY_ITEM, params -> createInventoryItem(params));
        commandHandlers.put(CREATE_STORE, params -> createStore(params));
        commandHandlers.put(GET_ALL_STORE, params -> getAllStore(params));

        // broadcast commands setup
        broadcastCommands.add(CREATE_STORE);
        broadcastCommands.add(CREATE_INVENTORY_ITEM);
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
        // always make sure the parameters have necessary values
        if (null == parameters.get(ERROR_IND)) {
            parameters.put(ERROR_IND, String.valueOf(false));
        }

        try {
            return commandHandlers.getOrDefault(command, params -> returnNoSuchCommand(command, params)).handle(parameters);
        } catch (Exception e) {
            e.printStackTrace();
            return returnError(parameters, e.getMessage());
        }
    }

    public boolean isBroadcasted(String command) {
        return broadcastCommands.contains(command);
    }
    
    private <T extends Storable> void persistStorable(Storer<T> storer, T storable) {
        try {
            storer.save(storable);
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist storable", e);
        }
    }

    public Map<String, String> createInventoryItem(Map<String, String> params) {
        InventoryItem item = new InventoryItem(SurrogateKeyManager.getInstance().nextKey(SurrogateKeyManager.INV_ITEM_SURROGATE_KEY));
        item.initialize(params);
        persistStorable(StorerFactory.getItemStorer(), item);
        return returnBoolean(params, true);
    }

    public Map<String, String> createStore(Map<String, String> params) {
        Store store = new Store(SurrogateKeyManager.getInstance()
            .nextKey(SurrogateKeyManager.STORE_SURROGATE_KEY));
        store.initialize(params);
        persistStorable(StorerFactory.getStoreStorer(), store);
        return returnBoolean(params, true);
    }

    public Map<String, String> getAllStore(Map<String, String> params) {
        Storer<Store> storeStorer = StorerFactory.getStoreStorer();
        List<Store> stores;
        try {
            stores = storeStorer.loadAllId(storeStorer.getIds());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load created stores", e);
        }
        return returnString(params, TransmissionHelper.toTransmissionString(stores));
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

}
