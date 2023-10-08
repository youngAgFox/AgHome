package com.ag.database;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Interface whose implementations can store and retrieve objects in a persistable manner.
 * 
 * @author asegedi
 */
public interface Storer<T extends Storable & Serializable> {

    /**
     * Returns a list of objects in range of the start and end index.
     * 
     * @param start the start index, inclusive
     * @param end the end index, exclusive
     * @return a list of all loaded objects.
     * @throws IOException on IO failure.
     * @throws IndexOutOfRangeException on bad start or end index.
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
     */
    public abstract void saveAll(List<T> objects) throws IOException;

    /**
     * Save a single object to the file.
     * <p>
     * After this operation completes the object is persisted to the file.
     * Retrieval of the object can be done using the {@link #load} or {@link #loadAll} methods.
     * 
     * @param object the object to save.
     * @throws IOException
     */
    public abstract void save(T object) throws IOException;

    /**
     * Load a particular object from the database using the objects id.
     * 
     * @param id the object to load via its id.
     * @return
     * @throws IOException
     */
    public abstract T load(long id) throws IOException;

    /**
     * Deletes the object from the storer. Whether or not the object information is wiped is at the implementation's
     * discretion. At most all that is required is that the id the object used be freed.
     * 
     * @param object
     * @throws IOException
     */
    public abstract void delete(T object) throws IOException;

    /**
     * Deletes the object from the storer. Whether or not the object information is wiped is at the implementation's
     * discretion. At most all that is required is that the id the object used be freed.
     * 
     * @param object
     * @throws IOException
     */
    public abstract void deleteId(long id) throws IOException;

    public abstract void deleteAll(List<T> objects) throws IOException;

    public abstract void deleteAllId(List<Long> ids) throws IOException;
}