package com.ag.Network;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ag.DynamicObject;
import com.ag.DynamicType;
import com.ag.database.FlatFileStorerFactory;
import com.ag.database.InventoryItem;
import com.ag.database.Storable;
import com.ag.database.Store;
import com.ag.database.Storer;
import com.ag.database.SurrogateKeyManager;
import com.ag.json.JsonParser;

public class ServerCommandHandler {

    // commands
    private static final String CREATE_INVENTORY_ITEM = "create_inv_item";
    private static final String CREATE_STORE = "create_store";
    private static final String GET_ALL_STORE = "get_all_store";
    private static final String GET_ALL_SHELF = "get_all_shelf";
    private static final String GET_ALL_SHELF_INV_ITEM = "get_all_shelf_inv_item";

    // argument keys
    public static final String VALUE = "value";
    public static final String COMMAND = "command";
    public static final String RETURN_VALUE = "value";
    public static final String ERROR_IND = "error_ind";
    public static final String ERROR_MESSAGE = "error_msg";
    public static final String ID = "id";

    private static ServerCommandHandler instance;

    private Map<String, CommandHandler> commandHandlers = new HashMap<>();
    private Set<String> broadcastCommands = new HashSet<>();

    private ServerCommandHandler() {
        // handlers setup
        commandHandlers.put(CREATE_INVENTORY_ITEM, args -> respondToCreateAndSaveStorable(args, InventoryItem.class));
        commandHandlers.put(CREATE_STORE, args -> respondToCreateAndSaveStorable(args, Store.class));
        commandHandlers.put(GET_ALL_STORE, args -> getAllStore(args));

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
        DynamicObject handle(DynamicObject args);
    }

    public DynamicObject handleRequest(DynamicObject request) {
        // always make sure the args have necessary values
        if (!request.containsKey(ERROR_IND)) {
            request.put(ERROR_IND, false);
        }

        try {
            return commandHandlers.getOrDefault(request.get(COMMAND), args -> returnNoSuchCommand(args)).handle(request);
        } catch (Exception e) {
            e.printStackTrace();
            return setError(request, e.getMessage());
        }
    }

    public boolean isBroadcasted(DynamicObject args) {
        return broadcastCommands.contains(args.get(COMMAND));
    }
    
    @SuppressWarnings("unchecked")
    private <T extends Storable> void persistStorable(T storable) {
        try {
            Storer<T> storer = (Storer<T>) FlatFileStorerFactory.getStorer(storable.getClass());
            storer.save(storable);
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist storable", e);
        }
    }

    public <T extends Storable> T createAndSaveStorable(Class<T> storeableType, DynamicObject args) {
        Long key = SurrogateKeyManager.getInstance().nextKey(storeableType);
        try {
            T storable = storeableType.getConstructor(long.class).newInstance(key);
            JsonParser parser = new JsonParser();
            parser.initializeInstance(storable, args);
            persistStorable(storable);
            return storable;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create storable", e);
        }
    }

    public <T extends Storable> DynamicObject respondToCreateAndSaveStorable(DynamicObject args, Class<T> storableClass) {
        Storable stored = createAndSaveStorable(storableClass, args);
        return args.put(ERROR_IND, null == stored).put(ID, stored.getId());
    }

    public DynamicObject getAllStore(DynamicObject args) {
        Storer<Store> storeStorer = FlatFileStorerFactory.getStorer(Store.class);
        List<Store> stores;
        try {
            stores = storeStorer.loadAllId(storeStorer.getIds());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load created stores", e);
        }
        DynamicObject storesArray = new DynamicObject(DynamicType.ARRAY, stores);
        return args.put(VALUE, storesArray);
    }

    private DynamicObject returnNoSuchCommand(DynamicObject args) {
        return setError(args, "Unknown command: '" + args.get(COMMAND) + "'");
    }

    private DynamicObject setError(DynamicObject args, String message) {
        args.put(ERROR_IND, true);
        args.put(ERROR_MESSAGE, message);
        return args;
    }

}
