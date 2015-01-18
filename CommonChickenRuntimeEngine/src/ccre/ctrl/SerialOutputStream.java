package ccre.ctrl;

import java.io.IOException;
import java.io.OutputStream;

import ccre.channel.SerialOutput;

public class SerialOutputStream extends OutputStream {
    private final SerialOutput output;

    public SerialOutputStream(SerialOutput output) {
        this.output = output;
    }

    @Override
    public void write(int b) throws IOException {
        output.writeFully(new byte[] {(byte) b}, 0, 1);
    }

    public void write(byte b[], int off, int len) throws IOException {
        output.writeFully(b, off, off + len);
    }

    public void flush() throws IOException {
        output.flush();
    }

    public void close() throws IOException {
        output.close();
    }
}
