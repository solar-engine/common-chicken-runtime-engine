package ccre.ctrl;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import ccre.channel.SerialIO;

public class LoopbackSerialIO implements SerialIO {
    
    public LoopbackSerialIO(BlockingQueue<byte[]> source, BlockingQueue<byte[]> target) {
        this.source = source;
        this.target = target;
    }

    public LoopbackSerialIO(BlockingQueue<byte[]> data) {
        this.source = this.target = data;
    }
    
    public LoopbackSerialIO() {
        this(new ArrayBlockingQueue<byte[]>(16));
    }
    
    private Character termination = null;
    private final BlockingQueue<byte[]> source, target;
    private byte[] reading;
    private int readingIndex;
    private final ByteBuffer writing = ByteBuffer.allocate(1024);
    private boolean flushOnWrite = true;

    public void setTermination(Character end) throws IOException {
        termination = end;
    }

    public byte[] readBlocking(int max) throws IOException {
        return read(max, true);
    }

    public byte[] readNonblocking(int max) throws IOException {
        return read(max, false);
    }
    
    private synchronized byte[] read(int max, boolean canBlock) throws IOException {
        if (reading != null && readingIndex >= reading.length) {
            reading = null;
        }
        if (reading == null) {
            readingIndex = 0;
            try {
                reading = canBlock ? source.take() : source.poll();
            } catch (InterruptedException e) {
                throw new InterruptedIOException("interrupted during blocking LoopbackSerialIO read.");
            }
            if (reading == null) {
                return new byte[0];
            }
        }
        byte[] out;
        if (readingIndex == 0 && reading.length <= max) {
            out = reading;
            reading = null;
        } else {
            out = Arrays.copyOfRange(reading, readingIndex, Math.min(max, reading.length));
            readingIndex += out.length;
        }
        return out;
    }

    public synchronized void flush() throws IOException {
        if (writing.position() > 0) {
            byte[] toQueue = new byte[writing.position()];
            writing.get(toQueue);
            try {
                target.put(toQueue);
            } catch (InterruptedException e) {
                throw new InterruptedIOException("interrupted during blocking LoopbackSerialIO write.");
            }
            writing.clear();
        }
    }

    public void setFlushOnWrite(boolean flushOnWrite) throws IOException {
        this.flushOnWrite = flushOnWrite;
    }

    public void writeFully(byte[] bytes) throws IOException {
        write(bytes, false);
    }
    
    private int write(byte[] bytes, boolean partial) throws IOException {
        if (bytes.length == 0) {
            return 0;
        }
        int offset = 0;
        while (writing.remaining() < bytes.length - offset) {
            if (writing.hasRemaining()) {
                int count = writing.remaining();
                writing.put(bytes, offset, count);
                offset += count;
            }
            flush();
            if (partial && offset > 0) {
                return offset;
            }
        }
        if (offset < bytes.length) {
            writing.put(bytes, offset, bytes.length - offset);
        }
        if (flushOnWrite || !writing.hasRemaining()) {
            flush();
        }
        return bytes.length;
    }

    public int writePartial(byte[] bytes) throws IOException {
        return write(bytes, true);
    }
}
