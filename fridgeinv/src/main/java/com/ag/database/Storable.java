package com.ag.database;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Interface for storing and retrieving objects from a file.
 * 
 * @author asegedi
 */
public abstract class Storable<T> {

    protected File file;

    /**
     * Creates a Storable and associates it with a given File object.
     * 
     * @param file the file to use to persist and load the Storable.
     */
    public Storable(File file) {
        this.file = file;
    }

    /**
     * Returns a list of objects in range of the start and end index.
     * 
     * @param start the start index, inclusive
     * @param end the end index, exclusive
     * @return a list of all loaded objects.
     * @throws IOException on IO failure.
     */
    public abstract List<T> loadAll(long start, long end) throws IOException;

    /**
     * The number of saved entries in this Storable.
     * 
     * @return the number of saved entries.
     * @throws IOException on IO failure.
     */
    public abstract long savedEntries() throws IOException;

    /**
     * Saves all provided objects. 
     * <p>
     * All saved objects are consecutive.
     * After this operation completes, objects are persisted, and termination of the program will
     * allow retrieval of the objects using the {@link #load} or {@link #loadAll} methods.
     * 
     * @param objects the list of objects to save.
     * @return true on successful save, false otherwise.
     */
    public abstract boolean saveAll(List<T> objects) throws IOException;

    /**
     * Save a single object to the file.
     * <p>
     * After this operation completes the object is persisted to the file.
     * Retrieval of the object can be done using the {@link #load} or {@link #loadAll} methods.
     * 
     * @param object the object to save.
     * @return
     * @throws IOException
     */
    public abstract boolean save(T object) throws IOException;

    /**
     * Load a particular object from the database using the objects id.
     * 
     * @param id the object to load via its id.
     * @return
     * @throws IOException
     */
    public abstract T load(long id) throws IOException;

    /**
     * Returns a unique ID for the object.
     * 
     * @param object the object to obtain the id of.
     * @return the unique ID that represents the object.
     */
    public abstract long getId(T object);
}