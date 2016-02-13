/*
 * Copyright 2016 Cel Skeggs
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
package ccre.recording;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.function.Function;

class StreamDecoder {
    private final InputStream in;

    public StreamDecoder(InputStream in) throws IOException {
        this.in = in;
        byte[] expected = StreamEncoder.MAGIC_STRING.getBytes();
        byte[] got = new byte[expected.length];
        int i = 0;
        while (i < got.length) {
            int o = in.read(got, i, got.length - i);
            if (o == -1) {
                throw new EOFException();
            }
            i += o;
        }
        if (!Arrays.equals(expected, got)) {
            throw new IOException("Invalid header: is this really a recorded stream, and is it of the correct version?");
        }
    }

    private int readUnsigned() throws IOException {
        int i = in.read();
        if (i == -1) {
            throw new EOFException();
        }
        return i;
    }

    private int read16() throws IOException {
        int i = readUnsigned();
        return (i << 8) | readUnsigned();
    }

    private int read32() throws IOException {
        int i = read16();
        return (i << 16) | read16();
    }

    private long read64() throws IOException {
        long i = read32();
        return (i << 32) | (read32() & 0xFFFFFFFFL);
    }

    // ******* VARINT IMPLEMENTATIONS *******
    // need to be synchronized with StreamEncoder!

    // in units of 10 microseconds
    private int readTimeDeltaWithPrefetchedByte(int b0) throws IOException {
        if ((b0 & 0x80) == 0) {
            // bit format: 0xxxxxxx, offset 0
            return b0;
        } else {
            int b1 = readUnsigned();
            if (b0 == 0xFF && b1 == 0xFF) {
                // bit format: 11111111 11111111 xxxxxxxx xxxxxxxx xxxxxxxx
                // xxxxxxxx, offset 0
                return read32();
            } else {
                // bit format: 1xxxxxxx xxxxxxxx, offset 128
                int value = ((b0 & 0x7F) << 8) | b1;
                return value + 128;
            }
        }
    }

    private int readChannelNumber() throws IOException {
        int b0 = readUnsigned();
        if ((b0 & 0x80) == 0) {
            // 0xxxxxxx, offset 0
            return b0;
        } else if ((b0 & 0x40) == 0) {
            // 10xxxxxx, offset 128
            return b0;
        } else if ((b0 & 0x20) == 0) {
            // 110xxxxx, offset 196
            return b0;
        } else if ((b0 & 0x10) == 0) {
            // 1110xxxx, offset 224
            return b0;
        } else {
            int b1 = readUnsigned();
            if (b0 == 0xFF && b1 == 0xFF) {
                // 11111111 11111111 xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx
                return read32();
            }
            int value = ((b0 & 0x0F) << 8) | b1;
            // 1111xxxx xxxxxxxx, offset 240
            return value + 240;
        }
    }

    private int readArrayLength() throws IOException {
        int b0 = readUnsigned();
        if (b0 != 255) {
            return b0;
        } else {
            return read32();
        }
    }

    private long readGeneralVarInt() throws IOException {
        // needs one to ten bytes, depending on the value
        // first, read an unsigned integer with MSB continuation
        long l = 0;
        int b;
        do {
            b = readUnsigned();
            l = (l << 7) | (b & 0x7F);
        } while ((b & 0x80) != 0);
        // now disjoin the positives and negatives
        if ((l & 1) != 0) {
            // >>> instead of >> so that we don't get any pesky sign-extension
            return ~(l >>> 1);
        } else {
            return l >>> 1;
        }
    }

    private long lastTimestamp = 0;

    public RecordSnapshot decode(Function<Integer, Byte> channelNumberToSnapshotType) throws IOException {
        RecordSnapshot rs = new RecordSnapshot();
        int b0 = in.read();
        if (b0 == -1) {
            return null; // END OF STREAM
        }
        lastTimestamp += readTimeDeltaWithPrefetchedByte(b0);
        rs.timestamp = lastTimestamp;
        rs.channel = readChannelNumber();
        Byte b = channelNumberToSnapshotType.apply(rs.channel);;
        if (b == null) {
            throw new IOException("Uninitialized channel: " + rs.channel);
        }
        rs.type = b;
        switch (b) {
        case RecordSnapshot.T_NULL:
            // nothing else
            break;
        case RecordSnapshot.T_BYTE:
            rs.value = readUnsigned();
            break;
        case RecordSnapshot.T_SHORT:
            rs.value = read16();
            break;
        case RecordSnapshot.T_INT:
            rs.value = read32();
            break;
        case RecordSnapshot.T_LONG:
            rs.value = read64();
            break;
        case RecordSnapshot.T_VARINT:
            rs.value = readGeneralVarInt();
            break;
        case RecordSnapshot.T_BYTES:
            rs.data = new byte[readArrayLength()];
            in.read(rs.data);
            break;
        default:
            throw new RuntimeException("Invalid type for StreamDecoder: " + rs.type);
        }
        return rs;
    }

    public void close() throws IOException {
        in.close();
    }
}
