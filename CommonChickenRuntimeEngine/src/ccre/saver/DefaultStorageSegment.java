/*
 * Copyright 2013-2015 Colby Skeggs
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
public final class DefaultStorageSegment extends StorageSegment {

    private final CHashMap<String, String> data = new CHashMap<String, String>();
    private String name;
    private boolean modified = false;
    private final StorageProvider provider;

    /**
     * Load a map from a properties-like file.
     * 
     * @param input the InputStream to read from.
     * @param keepInvalidLines whether or not to save invalid lines under backup
     * keys.
     * @param target the map to put the loaded keys into.
     * @throws IOException if reading from the input fails for some reason.
     */
    public static void loadProperties(InputStream input, boolean keepInvalidLines, CHashMap<String, String> target) throws IOException {
        BufferedReader din = new BufferedReader(new InputStreamReader(input));
        try {
            while (true) {
                String line = din.readLine();
                if (line == null) {
                    break;
                }
                int ind = line.indexOf('=');
                if (ind == -1) { // Invalid or empty line.
                    if (!line.isEmpty() && keepInvalidLines) {
                        Logger.warning("Invalid line ignored in configuration: " + line + " - saving under backup key.");
                        target.put(UniqueIds.global.nextHexId("unknown-" + System.currentTimeMillis() + "-" + line.hashCode()), line);
                    }
                    continue;
                }
                String key = line.substring(0, ind), value = line.substring(ind + 1);
                target.put(key, value);
            }
        } finally {
            din.close();
        }
    }

    DefaultStorageSegment(String name, StorageProvider provider) {
        this.provider = provider;
        if (name == null) {
            throw new NullPointerException();
        }
        for (char c : name.toCharArray()) {
            if (!(Character.isUpperCase(c) || Character.isLowerCase(c) || Character.isDigit(c) || c == '$' || c == '_')) {
                throw new IllegalArgumentException("Storage names must only contain 'a-zA-Z0-9$_', but got: " + c);
            }
        }
        this.name = name;
        try {
            InputStream target = provider.openInputFile("ccre_storage_" + name);
            if (target == null) {
                Logger.info("No data file for: " + name + " - assuming empty.");
            } else {
                try {
                    loadProperties(target, true, data);
                } finally {
                    target.close();
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
                PrintStream pout = new PrintStream(provider.openOutputFile("ccre_storage_" + name));
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
