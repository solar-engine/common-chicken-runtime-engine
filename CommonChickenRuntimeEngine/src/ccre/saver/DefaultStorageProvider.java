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

import ccre.log.LogLevel;
import ccre.log.Logger;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * The default storage provider. This will store data in the subdirectory
 * 'ccre_storage' of the current directory, using builtin java.io classes. This
 * is not suitable for running on Squawk.
 *
 * @author skeggsc
 */
class DefaultStorageProvider extends StorageProvider {

    @Override
    protected StorageSegment open(String name) {
        return new DefaultStorageSegment(name);
    }

    /**
     * Implementation detail.
     */
    private static class DefaultStorageSegment extends HashMappedStorageSegment {

        protected String name;
        protected boolean modified = false;

        DefaultStorageSegment(String name) {
            this.name = name;
            try {
                DataInputStream din = new DataInputStream(new BufferedInputStream(new FileInputStream("ccre_storage/" + name)));
                try {
                    synchronized (this) {
                        while (true) {
                            String key = din.readUTF();
                            if (key.isEmpty()) {
                                break;
                            }
                            byte[] bytes = new byte[din.readShort() & 0xFFFF];
                            din.readFully(bytes);
                            data.put(key, bytes);
                        }
                    }
                } finally {
                    din.close();
                }
            } catch (FileNotFoundException ex) {
                Logger.info("No data file for: " + name + " - assuming empty.");
                // No data by default. Do nothing
            } catch (IOException ex) {
                Logger.log(LogLevel.WARNING, "Error reading storage: " + name, ex);
            }
        }

        @Override
        public synchronized void setBytesForKey(String key, byte[] bytes) {
            super.setBytesForKey(key, bytes);
            modified = true;
        }

        @Override
        public synchronized void flush() {
            if (modified) {
                try {
                    File f = new File("ccre_storage");
                    if (!f.exists()) {
                        f.mkdir();
                    }
                    DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("ccre_storage/" + name)));
                    try {
                        for (String key : data) {
                            byte[] bytes = data.get(key);
                            dout.writeUTF(key);
                            if ((short) (bytes.length) != bytes.length) {
                                throw new IOException("Value cannot fit in 65535 bytes!");
                            }
                            dout.writeShort(bytes.length);
                            dout.write(bytes);
                        }
                        dout.writeUTF("");
                    } finally {
                        dout.close();
                    }
                } catch (IOException ex) {
                    Logger.log(LogLevel.WARNING, "Error writing storage: " + name, ex);
                }
                modified = false;
            }
        }

        @Override
        public void close() {
            flush();
        }
    }
}
