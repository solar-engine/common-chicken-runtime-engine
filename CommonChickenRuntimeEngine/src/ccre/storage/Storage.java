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
package ccre.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The holder for the current storage provider, and the superclass for any
 * storage providers.
 *
 * @author skeggsc
 */
public class Storage {

    private static File basedir = new File(".");

    /**
     * Set the backing folder.
     *
     * @param basedir the directory to store CCRE data in.
     */
    public static void setBaseDir(File basedir) {
        Storage.basedir = basedir;
    }

    /**
     * Open an output stream to the specified file.
     *
     * @param name The filename.
     * @return The output stream to that file.
     * @throws java.io.IOException If an error occurs while opening an output.
     */
    public static OutputStream openOutput(String name) throws IOException {
        return new FileOutputStream(new File(basedir, name));
    }

    /**
     * Open an input stream from the specified file, or NULL if it doesn't
     * exist.
     *
     * @param name The filename.
     * @return The input stream from the file, or null if it doesn't exist.
     * @throws java.io.IOException If an error occurs while opening the file
     * besides the file not existing.
     */
    public static InputStream openInput(String name) throws IOException {
        try {
            return new FileInputStream(new File(basedir, name));
        } catch (FileNotFoundException ex) {
            return null;
        }
    }

    /**
     * Open a StorageSegment for the specified name.
     *
     * @param name the name of the storage segment.
     * @return the StorageSegment that has been opened.
     */
    public static StorageSegment openStorage(String name) {
        return new StorageSegment(name);
    }
}
