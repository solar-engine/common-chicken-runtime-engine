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

import javax.microedition.io.Connector;

import ccre.log.Logger;

import com.sun.squawk.microedition.io.FileConnection;
import com.sun.squawk.platform.posix.LibCUtil;
import com.sun.squawk.platform.posix.natives.LibC;

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

    protected OutputStream openOutputFile(String name) throws IOException {
        return new IgneousCRIOFileOutput(name);
    }

    protected InputStream openInputFile(String name) throws IOException {
        FileConnection fc = (FileConnection) Connector.open("file:///" + name, Connector.READ);
        return fc.exists() ? fc.openInputStream() : null;
    }

    protected InputStream openInputFile_Custom(String name) throws IOException {
        // Testing needed before being put into production!
        // Should actually throw IOException if file found but could not be opened.
        int fd = LibC.INSTANCE.open(name, LibC.O_RDONLY, 0);
        if (fd == -1) {
            return null;
        }
        return new IgneousCRIOFileInput(fd);
    }

    private class IgneousCRIOFileInput extends InputStream {
        // Warning! This is very experimental!
        // This is needed because the usual implementations don't allow for multiple open files,
        // and I needed to have a log file always open.

        private final int fd;
        private boolean closed = false;
        private final byte[] singleRead = new byte[1];

        IgneousCRIOFileInput(int fd) {
            this.fd = fd;
        }

        private void ensureNotClosed() throws IOException {
            if (closed) {
                throw new IOException("Already closed!");
            }
        }

        public int read() throws IOException {
            if (read(singleRead, 0, 1) == -1) {
                return -1;
            } else {
                return singleRead[0];
            }
        }

        public int read(byte[] b, int off, int len) throws IOException {
            ensureNotClosed();
            if (b == null) {
                throw new NullPointerException();
            } else if (off < 0 || len < 0 || off + len > b.length) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            } else if (off != 0) {
                byte[] data = new byte[len];
                int out = read(data, 0, len);
                if (out > 0) {
                    System.arraycopy(data, 0, b, off, out);
                }
                return out;
            }
            int out = LibC.INSTANCE.read(fd, b, len);
            if (out == -1) {
                throw new IOException("Could not read bytes from file: errno " + LibCUtil.errno());
            } else if (out == 0) {
                return -1;
            }
            return out;
        }

        public long skip(long n) throws IOException {
            ensureNotClosed();
            int initial = LibC.INSTANCE.lseek(fd, 0, LibC.SEEK_CUR);
            if (initial == -1) {
                throw new IOException("Could not skip bytes in file: errno " + LibCUtil.errno());
            }
            int out = LibC.INSTANCE.lseek(fd, n, LibC.SEEK_CUR);
            if (out == -1) {
                throw new IOException("Could not skip bytes in file: errno " + LibCUtil.errno());
            }
            return out - initial;
        }

        public void close() throws IOException {
            if (!closed) {
                closed = true;
                if (LibC.INSTANCE.close(fd) == -1) {
                    throw new IOException("Could not close file: errno " + LibCUtil.errno());
                }
            }
        }

    }

    private class IgneousCRIOFileOutput extends OutputStream {
        // Warning! This is very experimental!
        // This is needed because the usual implementations don't allow for multiple open files,
        // and I needed to have a log file always open.

        private final int fd;
        private boolean closed = false;

        IgneousCRIOFileOutput(String filename) throws IOException {
            this.fd = LibC.INSTANCE.open("/" + filename, LibC.O_CREAT | LibC.O_WRONLY | LibC.O_EXCL, 0666);
            if (fd == -1) {
                throw new IOException("Could not open file to write: errno " + LibCUtil.errno());
            }
        }

        public void write(int b) throws IOException {
            write(new byte[] { (byte) b });
        }

        public synchronized void write(byte[] b, int off, int len) throws IOException {
            if (b == null) {
                throw new NullPointerException();
            }
            ensureArrayReferenceInRange(off, len, b);
            ensureNotClosed();
            if (len == 0) {
                return;
            }
            byte[] actualBuffer;
            if (off != 0) {
                actualBuffer = new byte[len];
                System.arraycopy(b, off, actualBuffer, 0, len);
            } else {
                actualBuffer = b;
            }
            int count = LibC.INSTANCE.write(fd, actualBuffer, len);
            if (count < 0) {
                throw new IOException("Could not write: errno " + LibCUtil.errno() + " for " + count);
            } else if (count > len || count == 0) {
                throw new IOException("Could not write: " + count + " bad for " + len);
            } else if (count < len) {
                write(b, off + count, len - count); // Try again!
            }
        }

        private void ensureArrayReferenceInRange(int off, int len, byte[] b) throws IllegalArgumentException {
            if (off < 0 || len < 0 || off + len > b.length) {
                throw new IllegalArgumentException();
            }
        }

        private void ensureNotClosed() throws IOException {
            if (closed) {
                throw new IOException("Already closed!");
            }
        }

        public synchronized void close() throws IOException {
            if (closed) {
                return;
            }
            closed = true;
            int out = LibC.INSTANCE.close(fd);
            if (out != 0) {
                throw new IOException("Could not close: " + out + " errno " + LibCUtil.errno());
            }
        }

        public synchronized void flush() throws IOException {
            ensureNotClosed();
            int out = LibC.INSTANCE.fsync(fd);
            if (out != 0) {
                throw new IOException("Could not close: " + out + " errno " + LibCUtil.errno());
            }
        }
    }
}
