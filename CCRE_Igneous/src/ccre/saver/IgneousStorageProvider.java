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

import ccre.downgrade.Iterator;
import ccre.log.*;
import com.sun.squawk.microedition.io.FileConnection;
import com.sun.squawk.platform.posix.LibCUtil;
import com.sun.squawk.platform.posix.natives.LibC;
import java.io.*;
import javax.microedition.io.Connector;

/**
 * A storage provider that works on Squawk and the FRC robot. This is because
 * Java ME doesn't use the same interface as Java SE.
 *
 * @author skeggsc
 */
public class IgneousStorageProvider extends StorageProvider {

    /**
     * Has this yet been registered as the storage provider?
     */
    private static boolean registered = false;

    /**
     * Ensure that this is registered as the storage provider. A warning will be
     * logged if this is called a second time.
     */
    public static void register() {
        if (registered) {
            Logger.warning("IgneousStorageProvider already registered!");
            return;
        }
        registered = true;
        StorageProvider.provider = new IgneousStorageProvider();
    }

    protected StorageSegment open(String name) {
        return new IgneousStorageSegment(name);
    }

    protected OutputStream openOutputFile(String string) throws IOException {
        // Warning! This is very experimental! This is needed because the usual implementations don't allow for multiple open files, and I needed to have a log file always open.
        final LibC c = LibC.INSTANCE;
        final int fd = c.open("/" + string, LibC.O_CREAT | LibC.O_WRONLY | LibC.O_EXCL, 0666);
        if (fd == -1) {
            throw new IOException("Could not open file to write: errno " + LibCUtil.errno());
        }
        return new OutputStream() {
            boolean closed = false;

            public void write(int b) throws IOException {
                write(new byte[]{(byte) b});
            }

            public synchronized void write(byte[] b, int off, int len) throws IOException {
                if (b == null) {
                    throw new NullPointerException();
                }
                if (off < 0 || len < 0 || off + len > b.length) {
                    throw new IllegalArgumentException();
                }
                if (closed) {
                    throw new IOException("Already closed!");
                }
                if (len == 0) {
                    return;
                }
                if (off != 0) {
                    byte[] na = new byte[len];
                    System.arraycopy(b, off, na, 0, len);
                    b = na;
                }
                int count = c.write(fd, b, len);
                if (count < 0) {
                    throw new IOException("Could not write: errno " + LibCUtil.errno() + " for " + count);
                } else if (count > len || count == 0) {
                    throw new IOException("Could not write: " + count + " bad for " + len);
                } else if (count < len) {
                    write(b, off + count, len - count); // Try again!
                }
            }

            public synchronized void close() throws IOException {
                if (closed) {
                    return;
                }
                closed = true;
                int out = c.close(fd);
                if (out != 0) {
                    throw new IOException("Could not close: " + out + " errno " + LibCUtil.errno());
                }
            }

            public synchronized void flush() throws IOException {
                if (closed) {
                    throw new IOException("Already closed!");
                }
                int out = c.fsync(fd);
                if (out != 0) {
                    throw new IOException("Could not close: " + out + " errno " + LibCUtil.errno());
                }
            }
        };
        /*FileConnection fconn = (FileConnection) Connector.open("file:///" + string, Connector.WRITE);
         fconn.create();
         return fconn.openOutputStream();*/
    }

    protected InputStream openInputFile(String string) throws IOException {
        FileConnection fc = (FileConnection) Connector.open("file:///" + string, Connector.READ);
        return fc.exists() ? fc.openInputStream() : null;
    }

    private static class IgneousStorageSegment extends HashMappedStorageSegment {

        protected String name, fname;
        protected boolean modified = false;

        IgneousStorageSegment(String name) {
            this.fname = "file:///storage-" + name + ".txt";
            this.name = name;
            try {
                FileConnection fc = (FileConnection) Connector.open(fname, Connector.READ);
                if (!fc.exists()) {
                    // No data by default. Do nothing
                    Logger.info("No data file for: " + name + " - assuming empty.");
                } else {
                    DataInputStream din = fc.openDataInputStream();
                    try {
                        synchronized (this) {
                            while (true) {
                                String key = din.readUTF();
                                if (key.length() == 0) {
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
                }
            } catch (IOException ex) {
                Logger.log(LogLevel.WARNING, "Error reading storage: " + name, ex);
            }
        }

        public synchronized void setBytesForKey(String key, byte[] bytes) {
            super.setBytesForKey(key, bytes);
            modified = true;
        }

        public synchronized void flush() {
            if (modified) {
                try {
                    FileConnection fconn = (FileConnection) Connector.open(fname, Connector.WRITE);
                    fconn.create();
                    DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(fconn.openOutputStream()));
                    try {
                        Iterator itr = data.iterator();
                        while (itr.hasNext()) {
                            String key = (String) itr.next();
                            byte[] bytes = (byte[]) data.get(key);
                            dout.writeUTF(key);
                            if ((bytes.length & 0xffff) != bytes.length) {
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

        public void close() {
            flush();
        }
    }
}
