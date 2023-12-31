package com.ag.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Stores objects in a file sequentially. Stores and retrieves file indices for objects and reads them in as needed.
 * Attempts to control the fragmentation of the file (empty spaces in the data) that comes from removal.
 */
public class FlatFileStorer<T extends Storable & Serializable> implements Storer<T> {

    private File metaInfoFile;
    private File objectFile;

    /**
     * The FlatFileStorer stores Storable objects in bit form in the filesystem. A call to {@link #open} must be done before
     * using any of the other operations.
     */
    public FlatFileStorer()  {

    }

    // Serves as a map from the unique ID of an object to the file position of that object.
    public Map<Long, FileIndex> objectFilePositions = new HashMap<>();

    /**
     * Associates this FlatFileStorer with the index and object files passed. If the files exists they will be read from and loaded.
     * 
     * @param metaFileName
     * @param objectFileName
     * @throws IOException
     */
    public void open(String metaFileName, String objectFileName) throws IOException {
        if (null == metaFileName || null == objectFileName) {
            throw new NullPointerException("Expect non-null index / object filename");
        }

        File indexFile = new File(metaFileName);
        File objectFile = new File(objectFileName);

        if (indexFile.equals(objectFile)) {
            throw new IOException("Cannot use the same file for index file and object file.");
        }

        Files.createDirectories(indexFile.getParentFile().toPath()); 
        Files.createDirectories(objectFile.getParentFile().toPath()); 

        this.metaInfoFile = indexFile;
        this.objectFile = objectFile;

        objectFilePositions = loadMetaInfo();
        System.out.println("Meta info loaded with " + objectFilePositions.size() + " entries.");
    }

    @SuppressWarnings("unchecked")
    /**
     * Loads n pairs of longs that define file offsets / size for each saved object.
     * 
     * @return the array of FileIndex objects.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private Map<Long, FileIndex> loadMetaInfo() throws IOException {
        if (null == metaInfoFile) {
            throw new IOException("Tried to load file meta data before successfully opening the meta data file");
        }
        if (!metaInfoFile.exists()) {
            return new HashMap<>();
        }
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(metaInfoFile))) {
            Object fileIndexMapObj = inputStream.readObject();
            // we save and load as array in order to overcome generic limitations
            return (Map<Long, FileIndex>) fileIndexMapObj;
        } catch (ClassNotFoundException e) {
            throw new IOException("The first object stored in the file '" + metaInfoFile.getName() + "' was not the FileIndex map");
        }
    }

    private void saveMetaInfo() throws FileNotFoundException, IOException {
        // overwrite the meta information 
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(metaInfoFile, false))) {
            outputStream.writeObject(objectFilePositions);
        }
    }

    @Override
    public List<T> loadAll(List<T> objects) throws IOException {
        List<T> persistedObjects = new ArrayList<>();
        for (T obj : objects) {
            persistedObjects.add(load(obj.getId()));
        }
        return persistedObjects;
    }

    @Override 
    public List<T> loadAllId(Set<Long> ids) throws IOException {
        List<T> persistedObjects = new ArrayList<>();
        for (Long id : ids) {
            persistedObjects.add(load(id));
        }
        return persistedObjects;
    }

    @Override
    public long savedEntries() throws IOException {
        return objectFilePositions.size();
    }

    @Override
    public void saveAll(List<T> objects) throws IOException {
        for (T obj : objects) {
            saveObject(obj);
        }
        saveMetaInfo();
    }

    @Override
    public void save(T object) throws IOException {
        saveObject(object);
        saveMetaInfo();
    }

    private void saveObject(T object) throws IOException {
        FileIndex fileIndex = objectFilePositions.get(object.getId());
        if (null == fileIndex) {
            // unpersisted object
            fileIndex = new FileIndex();
            objectFilePositions.put(object.getId(), fileIndex);
        }

        long newSize = getSerializedSize(object);
        boolean appendObject = newSize > fileIndex.size;

        // Add new objects to the end of the file.
        try (FileOutputStream fos = new FileOutputStream(objectFile, appendObject)) {
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            if (appendObject) {
                // when appending the object the current position is the end of the file
                fileIndex.position = fos.getChannel().position();
            } else {
                // set the position of the file to the position of the saved object
                fos.getChannel().position(fileIndex.position);
            }
            fileIndex.size = newSize;
            oos.writeObject(object);
        }
    }

    /**
     * Serializes the object but simply discards the bytes while counting them, returns the number of bytes that would be written.
     * 
     * @param object the object to serialize via the Serializable interface
     * @return the number of bytes that were written from serialization of the given object.
     */
    private long getSerializedSize(Object object) {
        OutputStreamCounter outCounter = new OutputStreamCounter();
        try (ObjectOutputStream sizeOutput = new ObjectOutputStream(outCounter)) {
            sizeOutput.writeObject(object);
        } catch (IOException ignored) {
            throw new RuntimeException("This should be impossible. ObjectOutputStream threw an IOException without performing IO.");
        }
        return outCounter.getTotalWrittenBytes();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T load(long id) throws IOException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(objectFile))) {
            ois.skip(objectFilePositions.get(id).position);
            return (T) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("The object file");
        }
    }

    @Override
    public void delete(T object) throws IOException {
        deleteId(object.getId());
    }

    @Override
    public void deleteId(long id) throws IOException {
        objectFilePositions.remove(id);
        saveMetaInfo();
    }

    @Override
    public void deleteAllId(List<Long> ids) throws IOException {
        for (Long id : ids) {
            objectFilePositions.remove(id);
        }
        saveMetaInfo();
    }

    @Override
    public void deleteAll(List<T> items) throws IOException {
        for (T item : items) {
            objectFilePositions.remove(item.getId());
        }
        saveMetaInfo();
    }

    @Override
    public Set<Long> getIds() {
        return Collections.unmodifiableSet(objectFilePositions.keySet());
    }

    @Override
    public boolean contains(Predicate<T> matches) throws IOException {
        for (Long id : getIds()) {
            if (matches.test(load(id))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean contains(Long id) {
        return getIds().contains(id);
    }

    @Override
    public T matches(Predicate<T> matcher) throws IOException {
        for (Long id : getIds()) {
            T obj = load(id);
            if (matcher.test(obj)) {
                return obj;
            }
        }
        return null;
    }

    @Override
    public List<T> allMatches(Predicate<T> matcher) throws IOException {
        List<T> matches = new ArrayList<>();
        for (Long id : getIds()) {
            T obj = load(id);
            if (matcher.test(obj)) {
                matches.add(obj);
            }
        }
        return matches;
    }
}
