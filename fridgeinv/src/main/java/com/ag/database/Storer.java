package com.ag.database;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Interface whose implementations can store and retrieve objects in a persistable manner.
 * 
 * @author asegedi
 */
public interface Storer<T extends Storable> {
    
    /**
     * Returns the persisted states of all passed objects
     * 
     * @param objects
     * @return a list of the persisted variants of the passed objects.
     * @throws IOException
     */
    public abstract List<T> loadAll(List<T> objects) throws IOException;

    /**
     * Returns the persisted states of all objects given by the passed ids.
     * @param ids
     * @return a list of the persisted variants of the passed object ids.
     * @throws IOException
     */
    public abstract List<T> loadAllId(Set<Long> ids) throws IOException;

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
     * Deletes the object with the given id from the storer. Whether or not the object information is wiped is at the implementation's
     * discretion. At most all that is required is that the id the object used be freed.
     * 
     * @param object
     * @throws IOException
     */
    public abstract void deleteId(long id) throws IOException;

    /**
     * Deletes all objects from the storer. Whether or not the object information is wiped is at the implementation's
     * discretion. At most all that is required is that the id the object used be freed.
     * 
     * @param objects
     * @throws IOException
     */
    public abstract void deleteAll(List<T> objects) throws IOException;

    /**
     * Deletes all objects with the given id from the storer. Whether or not the object information is wiped is at the implementation's
     * discretion. At most all that is required is that the id the object used be freed.
     * 
     * @param ids
     * @throws IOException
     */
    public abstract void deleteAllId(List<Long> ids) throws IOException;

    /**
     * Returns all the in use keys for this Storer. If this set does not contain a key, it has not been persisted yet.
     * @return all unique persisted keys.
     */
    public abstract Set<Long> getIds() throws IOException;

    /**
     * Returns true if any persisted objects match the predicate.
     * 
     * @param matches
     * @return true if any objects match the predicate.
     */
    public abstract boolean contains(Predicate<T> matches) throws IOException;

    /**
     * Returns true if the given id is persisted.
     * 
     * @param id
     * @return true if the id is persisted, false otherwise.
     */
    public abstract boolean contains(Long id) throws IOException;

    /**
     * Returns the first object that matches the given predicate. Searches over all persisted objects.
     * 
     * @param matches
     * @return the first matched object.
     */
    public abstract T matches(Predicate<T> matcher) throws IOException;

    /**
     * Returns all the objects that match the given predicate. Searches over all persisted objects.
     * 
     * @param matcher
     * @return 
     */
    public abstract List<T> allMatches(Predicate<T> matcher) throws IOException;
}