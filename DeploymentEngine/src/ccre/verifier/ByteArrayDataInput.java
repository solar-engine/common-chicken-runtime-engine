/*
 * Copyright 2016 Cel Skeggs.
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
package ccre.verifier;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

class ByteArrayDataInput implements DataInput {
    private final byte[] bytes;
    private final int offset;
    private final int length;
    private int index;

    public ByteArrayDataInput(byte[] bytes, int offset, int length) {
        this.bytes = bytes;
        this.offset = offset;
        this.length = length;
    }

    public ByteArrayDataInput(byte[] bytes) {
        this(bytes, 0, bytes.length);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws EOFException {
        if (off < 0 || len < 0 || off + len > b.length) {
            throw new IndexOutOfBoundsException();
        }
        if (len > length - index) {
            throw new EOFException();
        }
        System.arraycopy(bytes, offset + index, b, off, len);
        index += len;
    }

    @Override
    public int skipBytes(int len) {
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }
        len = Math.min(len, length - index);
        index += len;
        return len;
    }

    @Override
    public byte readByte() throws EOFException {
        if (index >= length) {
            throw new EOFException();
        }
        return bytes[offset + index++];
    }

    protected void pushbackOne() throws IOException {
        if (index <= 0) {
            throw new IOException("Cannot push back at start of stream!");
        }
        index--;
    }

    // and all the derived impls

    @Override
    public void readFully(byte[] b) throws EOFException {
        readFully(b, 0, b.length);
    }

    @Override
    public boolean readBoolean() throws EOFException {
        return readByte() != 0;
    }

    @Override
    public int readUnsignedByte() throws EOFException {
        return readByte() & 0xFF;
    }

    @Override
    public short readShort() throws EOFException {
        int a = readByte();
        int b = readByte();
        return (short) ((a << 8) | (b & 0xFF));
    }

    @Override
    public int readUnsignedShort() throws EOFException {
        return readShort() & 0xFFFF;
    }

    @Override
    public char readChar() throws EOFException {
        return (char) readShort();
    }

    @Override
    public int readInt() throws EOFException {
        int a = readByte() & 0xFF;
        int b = readByte() & 0xFF;
        int c = readByte() & 0xFF;
        int d = readByte() & 0xFF;
        return (a << 24) | (b << 16) | (c << 8) | d;
    }

    @Override
    public long readLong() throws EOFException {
        long a = readByte() & 0xFFL;
        long b = readByte() & 0xFFL;
        long c = readByte() & 0xFFL;
        long d = readByte() & 0xFFL;
        long e = readByte() & 0xFFL;
        long f = readByte() & 0xFFL;
        long g = readByte() & 0xFFL;
        long h = readByte() & 0xFFL;
        return (a << 56) | (b << 48) | (c << 40) | (d << 32) | (e << 24) | (f << 16) | (g << 8) | h;
    }

    @Override
    public float readFloat() throws EOFException {
        return Float.intBitsToFloat(readInt());
    }

    @Override
    public double readDouble() throws EOFException {
        return Double.longBitsToDouble(readLong());
    }

    @Override
    public String readLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = (char) readUnsignedByte();
            if (c == '\n') {
                break;
            } else if (c == '\r') {
                if (readUnsignedByte() != '\n') {
                    pushbackOne();
                }
                break;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @Override
    public String readUTF() throws IOException {
        return DataInputStream.readUTF(this);
    }
}
