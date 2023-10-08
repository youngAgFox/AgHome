package com.ag.database;

/**
 * Interface for objects that can be persisted.
 * 
 * @author asegedi
 */
public interface Storeable {

    /**
     * Returns a unique ID for this Storeable.
     * 
     * @return the unique ID that represents this object.
     */
    public abstract long getId();
}