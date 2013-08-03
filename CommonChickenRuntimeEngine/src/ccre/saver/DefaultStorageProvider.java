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
public class DefaultStorageProvider extends StorageProvider {

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
                            if ((bytes.length & 0xFFFF) != 0xFFFF) {
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
