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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import ccre.log.Logger;

class StreamEncoder {

    // We don't include the types - the decoder has to know them!
    // This will be done with a metadata channel.

    static final String MAGIC_STRING = "Encoded Recording Stream: version 0.1.0\n";
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final OutputStream real_output;
    private boolean closed = false;

    public StreamEncoder(OutputStream output) throws IOException {
        this.real_output = output;
        output.write(MAGIC_STRING.getBytes());
    }

    // ******* VARINT IMPLEMENTATIONS *******
    // need to be synchronized with StreamDecoder!

    // in units of 10 microseconds
    private void writeTimeDelta(int delta) {
        if (delta < 128) {
            // optimize the most for the very common case of up to 1.27 ms
            // encoded as a single byte
            // bit format: 0xxxxxxx
            out.write(delta < 0 ? 0 : (byte) delta);
        } else if (delta < 32895) {
            // optimize a bit less for the next most common case of up to 329 ms
            // bit format: 1xxxxxxx xxxxxxxx
            delta -= 128;
            out.write(0b1000_0000 | (delta >> 8));
            out.write(delta & 0xFF);
        } else {
            // don't both optimizing for this case; even if it happens, the
            // longer delays mean that the bitrate is still low even if
            // this is completely unoptimized.
            // bit format: 11111111 11111111 xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx
            out.write(0xFF);
            out.write(0xFF);
            out.write(delta >> 24);
            out.write(delta >> 16);
            out.write(delta >> 8);
            out.write(delta);
        }
    }

    private void writeChannelNumber(int channel) {
        // optimize for channel numbers less than around 4096
        if (channel < 0) {
            throw new IllegalArgumentException();
        } else if (channel < 128) {
            // 0xxxxxxx
            out.write(channel);
        } else if (channel < 196) {
            // 10xxxxxx
            out.write(0b1000_0000 | (channel - 128));
        } else if (channel < 224) {
            // 110xxxxx
            out.write(0b1100_0000 | (channel - 196));
        } else if (channel < 240) {
            // 1110xxxx
            out.write(0b1110_0000 | (channel - 224));
        } else if (channel < 4335) {
            // 1111xxxx xxxxxxxx
            channel -= 240; // to range 0-4094
            out.write(0b1111_0000 | (channel >> 8));
            out.write(channel & 0xFF);
        } else {
            // really don't bother optimizing for this many channels
            // 11111111 11111111 xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx
            out.write(0xFF);
            out.write(0xFF);
            out.write(channel >> 24);
            out.write(channel >> 16);
            out.write(channel >> 8);
            out.write(channel);
        }
    }

    private void writeArrayLength(int length) {
        if (length >= 0 && length < 255) {
            // optimize for the common case of short arrays
            out.write((byte) length);
        } else {
            // otherwise, it literally doesn't matter. The size of the data
            // overwhelms any difference that this might make.
            out.write(255);
            out.write((byte) (length >> 24));
            out.write((byte) (length >> 16));
            out.write((byte) (length >> 8));
            out.write((byte) length);
        }
    }

    private void writeGeneralVarInt(long value) {
        // needs one to ten bytes, depending on the value
        // unify range of small positive and small negative values so that
        // negative values aren't suddenly huge
        if (value < 0) {
            value = ((~value) << 1) | 1;
        } else {
            value <<= 1;
        }
        // now value should be treated as an unsigned integer!
        while (true) {
            int bits = (int) (value & 0x7F);
            value >>>= 7; // drop the seven bits we just got
            if (value == 0) {
                out.write(bits);
                break;
            } else {
                out.write(bits | 0x80);
            }
        }
    }

    private long lastTimestamp;

    public void encode(RecordSnapshot rs) throws IOException {
        if (closed) {
            throw new IOException("Already closed!");
        }
        writeTimeDelta((int) (rs.timestamp - lastTimestamp));
        writeChannelNumber(rs.channel);
        lastTimestamp = rs.timestamp;
        switch (rs.type) {
        case RecordSnapshot.T_NULL:
            // nothing else needed; 2 bytes common case
            break;
        case RecordSnapshot.T_BYTE:
            // 3 bytes common case
            out.write((byte) rs.value);
            break;
        case RecordSnapshot.T_SHORT:
            // 4 bytes common case
            out.write((byte) (rs.value >> 8));
            out.write((byte) rs.value);
            break;
        case RecordSnapshot.T_INT:
            // 6 bytes common case
            out.write((byte) (rs.value >> 24));
            out.write((byte) (rs.value >> 16));
            out.write((byte) (rs.value >> 8));
            out.write((byte) rs.value);
            break;
        case RecordSnapshot.T_LONG:
            // 10 bytes common case
            out.write((byte) (rs.value >> 56));
            out.write((byte) (rs.value >> 48));
            out.write((byte) (rs.value >> 40));
            out.write((byte) (rs.value >> 32));
            out.write((byte) (rs.value >> 24));
            out.write((byte) (rs.value >> 16));
            out.write((byte) (rs.value >> 8));
            out.write((byte) rs.value);
            break;
        case RecordSnapshot.T_VARINT:
            // common case varies significantly
            writeGeneralVarInt(rs.value);
            break;
        case RecordSnapshot.T_BYTES:
            // common case is 3 bytes + data length
            writeArrayLength(rs.data.length);
            out.write(rs.data, 0, rs.data.length);
            break;
        default:
            Logger.warning("Invalid type for StreamEncoder: " + rs.type);
            return;
        }
        if (out.size() >= 10000) {
            // flush at least once for every 10 KB, aka around every thousand to
            // five thousand samples.
            this.flush();
        }
    }

    public void flush() throws IOException {
        if (closed) {
            throw new IOException("Already closed!");
        }
        out.writeTo(real_output);
        out.reset();
    }

    public void close() throws IOException {
        if (closed) {
            return;
        }
        flush();
        closed = true;
        real_output.close();
    }
}
