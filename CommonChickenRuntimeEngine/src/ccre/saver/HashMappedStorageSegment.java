package ccre.saver;

import ccre.util.CHashMap;
import java.util.NoSuchElementException;

/**
 * A base of most StorageSegment implementations. Uses a CHashMap to keep track
 * of data until the data is written out to the disk or other medium.
 *
 * @author skeggsc
 */
public abstract class HashMappedStorageSegment extends StorageSegment {

    /**
     * The current data stored in this segment.
     */
    protected CHashMap<String, byte[]> data = new CHashMap<String, byte[]>();

    @Override
    public synchronized byte[] getBytesForKey(String key) throws NoSuchElementException {
        return data.get(key);
    }

    @Override
    public synchronized void setBytesForKey(String key, byte[] bytes) {
        data.put(key, bytes);
    }
}
