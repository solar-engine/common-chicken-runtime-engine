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
package ccre.obsidian;

import ccre.log.Logger;
import java.io.*;

/**
 * A utility class for Device Trees and other beaglebone utilities.
 *
 * @author skeggsc
 */
class DeviceTree {

    /**
     * Autocomplete the specified path. Search the given directory for a
     * filename that starts with the specified prefix, and then returns the
     * first one found. If none are found, it throws an IO Exception.
     *
     * @param directory The directory to search in.
     * @param prefix The prefix to look for.
     * @return The discovered file.
     * @throws IOException if there is not any file with
     */
    public static File autocompletePath(File directory, String prefix) throws IOException {
        String[] contents = directory.list();
        for (String cont : contents) {
            if (cont.startsWith(prefix)) {
                return new File(directory, cont);
            }
        }
        throw new IOException("Cannot autocomplete path: " + directory + " with prefix " + prefix);
    }

    /**
     * Load the specified cape manager. If it is already loaded, do nothing.
     *
     * @param name the cape manager name.
     * @throws IOException if this cannot be completed for any reason relating
     * to IO.
     */
    public static void loadCapeManager(String name) throws IOException {
        File capedir = autocompletePath(new File("/sys/devices"), "bone_capemgr");
        RandomAccessFile raf = new RandomAccessFile(new File(capedir, "slots"), "rw");
        try {
            while (true) {
                String line = raf.readLine();
                if (line == null) {
                    break;
                }
                if (line.contains(name)) {
                    return;
                }
            }
            raf.writeBytes(name);
        } finally {
            raf.close();
        }
        try {
            Thread.sleep(200); // Idea taken from load_device_tree of Adafruit BeagleBone python IO library
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Unload the specified cape manager.
     *
     * @param name the cape manager name.
     * @throws IOException if this cannot be completed for any reason relating
     * to IO.
     */
    public static boolean unloadCapeManager(String name) throws IOException {
        File capedir = autocompletePath(new File("/sys/devices"), "bone_capemgr");
        RandomAccessFile raf = new RandomAccessFile(new File(capedir, "slots"), "rw");
        try {
            while (true) {
                String line = raf.readLine();
                if (line == null) {
                    break;
                }
                if (line.contains(name)) {
                    int i = line.indexOf(':');
                    if (i == -1) {
                        throw new IOException("Cannot find colon to detect line for removal!");
                    }
                    String s = line.substring(0, i);
                    while (s.charAt(0) == ' ') {
                        s = s.substring(1); // TODO: Optimize
                    }
                    Logger.info("Writing: " + ("-" + s));
                    raf.writeBytes("-" + s);
                    return true;
                }
            }
        } finally {
            raf.close();
        }
        return false;
    }
}
