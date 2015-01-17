package ccre.ctrl;

import java.io.IOException;
import java.io.InterruptedIOException;

import ccre.channel.SerialIO;
import ccre.log.Logger;

public class DisconnectedSerialIO implements SerialIO {

    public void setTermination(Character end) throws IOException {
        // don't care.
    }

    public byte[] readBlocking(int max) throws IOException {
        Logger.warning("Blocking read from DisconnectedSerialIO!");
        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new InterruptedIOException("interrupted in DisconnectedSerialIO blocking read");
            }
        }
    }

    public byte[] readNonblocking(int max) throws IOException {
        return new byte[0];
    }

    public void flush() throws IOException {
        // do nothing.
    }

    public void setFlushOnWrite(boolean flushOnWrite) throws IOException {
        // don't care.
    }

    public void writeFully(byte[] bytes) throws IOException {
        // do nothing.
    }

    public int writePartial(byte[] bytes) throws IOException {
        return bytes.length;
    }
}
