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
package ccre.timeline;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;

import ccre.drivers.ByteFiddling;
import ccre.recording.Recorder;
import ccre.recording.Replayer;
import ccre.recording.Replayer.ReplaySample;
import ccre.time.Time;

/**
 * A timeline channel.
 *
 * @author skeggsc
 */
public class TimelineChannel {
    private final Replayer.ReplayChannel rpc;
    private static final float TICKS_PER_SECOND = Time.MICROSECONDS_PER_SECOND / 10f;
    private final long zero_stamp;
    private final List<String> options;
    private final WeakHashMap<byte[], String> outCache = new WeakHashMap<>();
    private float minFloat, maxFloat;

    /**
     * Creates a new TimelineChannel from a decoded channel.
     *
     * @param rpc the decoded channel.
     * @param zero_stamp the timestamp for time zero.
     */
    public TimelineChannel(Replayer.ReplayChannel rpc, long zero_stamp) {
        this.rpc = rpc;
        this.zero_stamp = zero_stamp;
        options = new ArrayList<>();
        if (rpc.type == Recorder.RawType.DISCRETE) {
            byte[] last = null;
            for (Iterator<ReplaySample> iterator = rpc.samples.iterator(); iterator.hasNext();) {
                Replayer.ReplaySample rs = iterator.next();
                if (rs.data != null && last != null && Arrays.equals(rs.data, last)) {
                    iterator.remove();
                }
                last = rs.data;
            }
            for (Replayer.ReplaySample rs : rpc.samples) {
                String s = new String(rs.data);
                if (!options.contains(s)) {
                    options.add(s);
                }
            }
        } else if (rpc.type == Recorder.RawType.FLOAT) {
            minFloat = Float.POSITIVE_INFINITY;
            maxFloat = Float.NEGATIVE_INFINITY;
            for (Replayer.ReplaySample rs : rpc.samples) {
                float f = Float.intBitsToFloat((int) rs.value);
                minFloat = Math.min(minFloat, f);
                maxFloat = Math.max(maxFloat, f);
            }
            System.out.println("MAX AND MIN: " + minFloat + "-" + maxFloat + " for " + rpc.name);
        }
    }

    /**
     * Returns the number of samples in the channel.
     *
     * @return the number of samples.
     */
    public int count() {
        return rpc.samples.size();
    }

    /**
     * Determines the absolute time, in seconds, for a certain sample.
     *
     * @param i the sample index.
     * @return the time, in seconds, based on the earliest sample from any
     * channel.
     */
    public float timeFor(int i) {
        return (rpc.samples.get(i).timestamp - zero_stamp) / TICKS_PER_SECOND;
    }

    /**
     * Determines the graph position that should be displayed for a certain
     * sample.
     *
     * @param i the sample index.
     * @return the graph position (y axis of the point) from -1.0 to +1.0
     */
    public float valueFor(int i) {
        switch (rpc.type) {
        case BOOLEAN:
            return rpc.samples.get(i).value != 0 ? 1 : -1;
        case EVENT:
        case OUTPUT_STREAM:
            return 0;
        case FLOAT:
            float value = Float.intBitsToFloat((int) rpc.samples.get(i).value);
            return 2 * (value - minFloat) / (maxFloat - minFloat) - 1;
        case DISCRETE:
            byte[] key = rpc.samples.get(i).data;
            if (!outCache.containsKey(key)) {
                outCache.put(key, new String(key));
            }
            return options.indexOf(outCache.get(key)) * 2f / (options.size() - 1) - 1;
        default:
            return -1; // TODO
        }
    }

    /**
     * Determines the absolute time of the earliest sample, or zero if there are
     * no samples.
     *
     * @return the beginning of the channel, in seconds.
     */
    public float beginAt() {
        return rpc.samples.isEmpty() ? 0 : (rpc.samples.get(0).timestamp - zero_stamp) / TICKS_PER_SECOND;
    }

    /**
     * Determines the absolute time of the last sample, or zero if there are no
     * samples.
     *
     * @return the end of the channel, in seconds.
     */
    public float endAt() {
        return rpc.samples.isEmpty() ? 0 : (rpc.samples.get(rpc.samples.size() - 1).timestamp - zero_stamp) / TICKS_PER_SECOND;
    }

    /**
     * Determines if this is a channel containing float data.
     *
     * @return true if a float channel, otherwise false.
     */
    public boolean isFloat() {
        return rpc.type == Recorder.RawType.FLOAT;
    }

    /**
     * Provides the human-readable (sort of) name for the channel.
     *
     * @return the name.
     */
    public String name() {
        return rpc.name;
    }

    /**
     * Determines the string to display next to a particular sample.
     *
     * @param i the sample index.
     * @return the text to display.
     */
    public String stringFor(int i) {
        switch (rpc.type) {
        case BOOLEAN:
            return Boolean.toString(rpc.samples.get(i).value != 0);
        case FLOAT:
            return Float.toString(Float.intBitsToFloat((int) rpc.samples.get(i).value));
        case EVENT:
            if (i < rpc.samples.size() - 1) {
                long time_delta = rpc.samples.get(i + 1).timestamp - rpc.samples.get(i).timestamp;
                return TimelinePanel.toTimeString(time_delta);
            }
            return "";
        case OUTPUT_STREAM:
        case DISCRETE:
            byte[] bytes = rpc.samples.get(i).data;
            if (!outCache.containsKey(bytes)) {
                outCache.put(bytes, Charset.forName("UTF-8").decode(ByteBuffer.wrap(bytes)) + " :" + ByteFiddling.toHex(bytes, 0, bytes.length));
            }
            return outCache.get(bytes);
        default:
            return "???"; // TODO
        }
    }

    /**
     * Determines the color to use to display the specified sample.
     *
     * @param i the sample index.
     * @return the color to display.
     */
    public Color colorFor(int i) {
        switch (rpc.type) {
        case BOOLEAN:
            return rpc.samples.get(i).value != 0 ? Color.GREEN : Color.RED;
        case FLOAT:
            float f = Float.intBitsToFloat((int) rpc.samples.get(i).value);
            if (f < 0) {
                return Renderer.blend(Color.RED, Color.BLACK, f + 1.0f);
            } else {
                return Renderer.blend(Color.BLACK, Color.GREEN, f);
            }
        case DISCRETE:
            byte[] key = rpc.samples.get(i).data;
            if (!outCache.containsKey(key)) {
                outCache.put(key, new String(key));
            }
            return Renderer.nthColor(options.indexOf(outCache.get(key)));
        case EVENT:
        case OUTPUT_STREAM:
        default:
            return Color.BLACK;
        }
    }

    /**
     * Determines if the channel should have a connecting line between
     * subsequent events to display the color.
     *
     * This will be true if the channel is a boolean channel or a discrete channel.
     *
     * @return true to draw a connecting line, false otherwise.
     */
    public boolean hasContinuationChannel() {
        return rpc.type == Recorder.RawType.BOOLEAN || rpc.type == Recorder.RawType.DISCRETE;
    }
}
