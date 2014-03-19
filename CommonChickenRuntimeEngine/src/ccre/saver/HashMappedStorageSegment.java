/*
 * Copyright 2013 Colby Skeggs
 * 
 * This file is part of the CCRE, the Common Chicken Runtime Engine.
 * 
 * The CCRE is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * The CCRE is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the CCRE.  If not, see <http://www.gnu.org/licenses/>.
 */
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
    protected final CHashMap<String, byte[]> data = new CHashMap<String, byte[]>();

    @Override
    public synchronized byte[] getBytesForKey(String key) throws NoSuchElementException {
        return data.get(key);
    }

    @Override
    public synchronized void setBytesForKey(String key, byte[] bytes) {
        data.put(key, bytes);
    }
}
