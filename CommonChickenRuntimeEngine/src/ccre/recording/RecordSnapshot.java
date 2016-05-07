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

import ccre.verifier.FlowPhase;

final class RecordSnapshot implements Comparable<RecordSnapshot> {
    public static final byte T_NULL = 0, T_BYTE = 1, T_SHORT = 2, T_INT = 3, T_LONG = 4, T_VARINT = 5, T_BYTES = 6;
    public long timestamp; // in ticks of 10 us
    public int channel;
    public byte type;
    public long value;
    public byte[] data;

    @FlowPhase
    public RecordSnapshot() {
    }

    @Override
    public int compareTo(RecordSnapshot o) {
        return Long.compare(timestamp, o.timestamp);
    }
}