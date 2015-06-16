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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ccre.log.Logger;

/**
 * The holder for the current storage provider, and the superclass for any
 * storage providers.
 *
 * @author skeggsc
 */
public abstract class StorageProvider {

    /**
     * The active storage provider.
     */
    private static StorageProvider provider;

    static synchronized void setProvider(StorageProvider provider) {
        if (StorageProvider.provider != null) {
            throw new IllegalStateException("StorageProvider already registered!");
        }
        StorageProvider.provider = provider;
    }

    /**
     * If a provider is not yet registered, register the default provider.
     */
    public static synchronized void initProvider() {
        if (provider == null) {
            try {
                provider = (StorageProvider) Class.forName("ccre.saver.DefaultStorageProvider").newInstance();
                return;
            } catch (Throwable t) {
                provider = new StorageProvider() {
                    @Override
                    protected OutputStream openOutputFile(String name) throws IOException {
                        throw new IOException("Cannot write to any files in a FakeStorageProvider!");
                    }

                    @Override
                    protected InputStream openInputFile(String name) throws IOException {
                        return null;
                    }
                };
                Logger.warning("No throwable printing provider!", t);
            }
        }
    }

    /**
     * Open an output stream to the specified file.
     *
     * @param name The filename.
     * @return The output stream to that file.
     * @throws java.io.IOException If an error occurs while opening an output.
     */
    public static OutputStream openOutput(String name) throws IOException {
        initProvider();
        return provider.openOutputFile(name);
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
        initProvider();
        return provider.openInputFile(name);
    }

    /**
     * Open a StorageSegment for the specified name.
     *
     * @param name the name of the storage segment.
     * @return the StorageSegment that has been opened.
     */
    public static StorageSegment openStorage(String name) {
        if (name == null) {
            throw new NullPointerException("Storage names cannot be null");
        }
        StringBuffer buf = new StringBuffer(name);
        for (int i = buf.length() - 1; i >= 0; i--) {
            char c = buf.charAt(i);
            if (c == ' ') {
                buf.setCharAt(i, '_');
            } else if (!(Character.isUpperCase(c) || Character.isLowerCase(c) || Character.isDigit(c))) {
                // escape any "weird" characters
                buf.setCharAt(i, '$');
                buf.insert(i + 1, (int) c);
            }
        }
        initProvider();
        return new DefaultStorageSegment(buf.toString(), provider);
    }

    /**
     * Open an OutputStream to the specified file.
     *
     * @param name The file name.
     * @return The OutputStream to the file.
     * @throws java.io.IOException If the file cannot be written to.
     */
    protected abstract OutputStream openOutputFile(String name) throws IOException;

    /**
     * Open an InputStream from the specified file, or return null if the file
     * does not exist.
     *
     * @param name The file name.
     * @return The InputStream from the file, or null if it doesn't exist.
     * @throws java.io.IOException If the file cannot be read from.
     */
    protected abstract InputStream openInputFile(String name) throws IOException;
}
