package ccre.ctrl;

import java.io.IOException;
import java.io.InterruptedIOException;

import ccre.channel.SerialIO;
import ccre.log.Logger;

public class DisconnectedSerialIO implements SerialIO {
    
    private boolean closed = false;

    public void setTermination(Character end) throws IOException {
        // don't care.
    }

    public synchronized byte[] readBlocking(int max) throws IOException {
        Logger.warning("Blocking read from DisconnectedSerialIO!");
        while (!closed) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                throw new InterruptedIOException("interrupted in DisconnectedSerialIO blocking read");
            }
        }
        throw new IOException("DisconnectedSerialIO closed.");
    }

    public byte[] readNonblocking(int max) throws IOException {
        if (closed) {
            throw new IOException("DisconnectedSerialIO closed.");
        }
        return new byte[0];
    }

    public void flush() throws IOException {
        if (closed) {
            throw new IOException("DisconnectedSerialIO closed.");
        }
        // do nothing.
    }

    public synchronized void close() throws IOException {
        // do nothing.
        closed = true;
        this.notifyAll();
    }

    public void setFlushOnWrite(boolean flushOnWrite) throws IOException {
        if (closed) {
            throw new IOException("DisconnectedSerialIO closed.");
        }
        // don't care.
    }

    public void writeFully(byte[] bytes, int from, int to) throws IOException {
        if (closed) {
            throw new IOException("DisconnectedSerialIO closed.");
        }
        // do nothing.
    }

    public int writePartial(byte[] bytes, int from, int to) throws IOException {
        if (closed) {
            throw new IOException("DisconnectedSerialIO closed.");
        }
        return to - from;
    }
}
