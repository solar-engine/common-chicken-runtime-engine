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

import java.util.List;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class Replayer {
    private final StreamDecoder decoder;

    public static final class ReplayChannel {
        public final String name;
        public final Recorder.RawType type;
        public final ArrayList<ReplaySample> samples = new ArrayList<>();

        public ReplayChannel(String name, Recorder.RawType type) {
            this.name = name;
            this.type = type;
        }

        private byte getSnapshotType() {
            switch (this.type) {
            case BOOLEAN:
                return RecordSnapshot.T_BYTE;
            case EVENT:
                return RecordSnapshot.T_NULL;
            case FLOAT:
                return RecordSnapshot.T_INT;
            case OUTPUT_STREAM:
                return RecordSnapshot.T_BYTES;
            case DISCRETE:
                return RecordSnapshot.T_BYTES;
            default:
                throw new RuntimeException();
            }
        }
    }

    public static final class ReplaySample {
        public final long timestamp;
        public final long value;
        public final byte[] data;

        public ReplaySample(RecordSnapshot snapshot) {
            this.timestamp = snapshot.timestamp;
            this.value = snapshot.value;
            this.data = snapshot.data;
        }
    }

    public Replayer(InputStream in) throws IOException {
        decoder = new StreamDecoder(in);
    }

    private final HashMap<Integer, ReplayChannel> channels = new HashMap<>();
    private final ArrayList<ReplayChannel> allChannels = new ArrayList<>();

    public List<ReplayChannel> decode() throws IOException {
        while (true) {
            RecordSnapshot snapshot = decoder.decode(this::extractType);
            if (snapshot == null) {
                break;
            }
            if (snapshot.channel == 0) {
                processMetaUpdate(snapshot);
            } else {
                ReplayChannel rc = channels.get(snapshot.channel);
                // must not be null because we got the type via extractType
                rc.samples.add(new ReplaySample(snapshot));
            }
        }
        return allChannels;
    }

    private void processMetaUpdate(RecordSnapshot snapshot) throws IOException {
        String d = new String(snapshot.data);
        if (d.isEmpty()) {
            throw new IOException("Invalid meta update of length 0!");
        }
        if (d.equals("\2ENDOFSTREAM")) {
            // we actually don't care in this decoder.
            return;
        }
        if (d.charAt(0) == '\0') {
            // init channel
            String[] strs = d.split("\0", 4);
            int new_channel_number;
            try {
                new_channel_number = Integer.parseInt(strs[1]);
            } catch (NumberFormatException ex) {
                throw new IOException("Invalid init channel meta update: invalid channel number format.");
            }
            Recorder.RawType rt;
            try {
                rt = Recorder.RawType.valueOf(strs[2]);
            } catch (IllegalArgumentException ex) {
                throw new IOException("Invalid init channel meta update: raw type name " + strs[2]);
            }
            String name = strs[3];
            if (channels.containsKey(new_channel_number)) {
                throw new IOException("Attempt to reinit channel!");
            }
            ReplayChannel rc = new ReplayChannel(name, rt);
            channels.put(new_channel_number, rc);
            allChannels.add(rc);
        } else if (d.charAt(0) == '\1') {
            // destroy channel
            int new_channel_number;
            try {
                new_channel_number = Integer.parseInt(d.substring(1));
            } catch (NumberFormatException ex) {
                throw new IOException("Invalid init channel meta update: invalid channel number format.");
            }
            if (channels.remove(new_channel_number) == null) {
                throw new IOException("Attempt to deinit nonexistent channel.");
            }
        } else {
            throw new IOException("Invalid meta update with initial byte " + d.charAt(0));
        }
    }

    private Byte extractType(int channel) {
        if (channel == 0) {
            return RecordSnapshot.T_BYTES;
        } else {
            ReplayChannel rc = channels.get(channel);
            return rc == null ? null : rc.getSnapshotType();
        }
    }
}
