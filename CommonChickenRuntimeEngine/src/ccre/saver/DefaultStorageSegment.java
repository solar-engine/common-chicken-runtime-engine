/*
 * Copyright 2013-2014 Colby Skeggs
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import ccre.log.Logger;
import ccre.util.CHashMap;
import ccre.util.UniqueIds;

/**
 * A default StorageSegment implementation.
 */
final class DefaultStorageSegment extends StorageSegment {

    private final CHashMap<String, String> data = new CHashMap<String, String>();
    private String name;
    private boolean modified = false;

    DefaultStorageSegment(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        this.name = name;
        try {
            InputStream target = StorageProvider.openInput("ccre_storage_" + name);
            if (target == null) {
                Logger.info("No data file for: " + name + " - assuming empty.");
            } else {
                BufferedReader din = new BufferedReader(new InputStreamReader(target));
                try {
                    while (true) {
                        String line = din.readLine();
                        if (line == null) {
                            break;
                        }
                        int ind = line.indexOf('=');
                        if (ind == -1) { // Invalid or empty line.
                            if (!line.isEmpty()) {
                                Logger.warning("Invalid line ignored in configuration: " + line + " - saving under backup key.");
                                data.put(UniqueIds.global.nextHexId("unknown-" + System.currentTimeMillis() + "-" + line.hashCode()), line);
                            }
                            continue;
                        }
                        String key = line.substring(0, ind), value = line.substring(ind + 1);
                        data.put(key, value);
                    }
                } finally {
                    din.close();
                }
            }
        } catch (IOException ex) {
            Logger.warning("Error reading storage: " + name, ex);
        }
    }

    @Override
    public synchronized String getStringForKey(String key) {
        return data.get(key);
    }

    @Override
    public synchronized void setStringForKey(String key, String bytes) {
        data.put(key, bytes);
        modified = true;
    }

    @Override
    public synchronized void flush() {
        if (modified) {
            try {
                PrintStream pout = new PrintStream(StorageProvider.openOutput("ccre_storage_" + name));
                try {
                    for (String key : data) {
                        if (key.contains("=")) {
                            Logger.warning("Invalid key ignored during save: " + key + " - saving under backup key.");
                            data.put(UniqueIds.global.nextHexId("badkey-" + System.currentTimeMillis() + "-" + key.hashCode()), key);
                        } else {
                            String value = data.get(key);
                            pout.println(key + "=" + value);
                        }
                    }
                } finally {
                    pout.close();
                }
            } catch (IOException ex) {
                Logger.warning("Error writing storage: " + name, ex);
            }
            modified = false;
        }
    }

    @Override
    public void close() {
        flush();
    }

    @Override
    public String getName() {
        return name;
    }
}
