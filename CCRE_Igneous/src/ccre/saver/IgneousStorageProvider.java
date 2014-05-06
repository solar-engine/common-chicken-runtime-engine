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
        StorageProvider.setProvider(new IgneousStorageProvider());
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
    }

    protected InputStream openInputFile(String string) throws IOException { // TODO: Make this have a similar implementation.
        FileConnection fc = (FileConnection) Connector.open("file:///" + string, Connector.READ);
        return fc.exists() ? fc.openInputStream() : null;
    }
}
