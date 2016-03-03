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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;

import ccre.behaviors.BehaviorArbitrator;
import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.ctrl.Faultable;
import ccre.ctrl.binding.ControlBindingCreator;
import ccre.discrete.DiscreteInput;
import ccre.discrete.DiscreteOutput;
import ccre.discrete.DiscreteType;
import ccre.log.Logger;
import ccre.storage.Storage;

/**
 * A class that handles channel-based data recording to an arbitrary
 * OutputStream.
 *
 * @author skeggsc
 */
public class Recorder {
    /**
     * The possible recording types.
     *
     * @author skeggsc
     */
    public static enum RawType {
        /**
         * A float channel.
         */
        FLOAT,
        /**
         * A boolean channel.
         */
        BOOLEAN,
        /**
         * An event channel.
         */
        EVENT,
        /**
         * An OutputStream.
         */
        OUTPUT_STREAM,
        /**
         * A discrete channel.
         */
        DISCRETE
    }

    private final ChanneledRecorder rec;
    private final AtomicInteger next_channel = new AtomicInteger();
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Creates a new recorder writing to this OutputStream. Remember that
     * writing to the stream will be synchronized, but still occur in a separate
     * thread.
     *
     * @param stream the output stream.
     * @throws IOException
     */
    public Recorder(OutputStream stream) throws IOException {
        this.rec = new ChanneledRecorder(stream);
    }

    /**
     * Closes and shuts down this recorder.
     *
     * @throws InterruptedException
     */
    public void close() throws InterruptedException {
        if (closed.compareAndSet(false, true)) {
            byte[] b = "\2ENDOFSTREAM".getBytes();
            rec.recordBytes(0, b, 0, b.length);
            rec.close();
        }
    }

    private int initChannel(RawType rawtype, String name) {
        if (closed.get()) {
            throw new IllegalStateException("Recorder is closed!");
        }
        if (name.indexOf('\0') != -1) {
            throw new IllegalArgumentException("Nulls not allowed.");
        }
        int channel_number = next_channel.incrementAndGet();
        byte[] b = ("\0" + channel_number + "\0" + rawtype.name() + "\0" + name).getBytes();
        rec.recordBytes(0, b, 0, b.length);
        return channel_number;
    }

    private void freeChannel(int channel_number) {
        if (closed.get()) {
            return; // don't bother
        }
        byte[] b = ("\1" + channel_number).getBytes();
        rec.recordBytes(0, b, 0, b.length);
    }

    /**
     * Creates a float output logging to this recorder.
     *
     * @param name the channel name.
     * @return the new float output.
     */
    public FloatOutput createFloatOutput(String name) {
        int channel = initChannel(RawType.FLOAT, name);
        return (f) -> {
            rec.recordInt(channel, Float.floatToIntBits(f));
        };
    }

    /**
     * Records a float input.
     *
     * @param input the input to record.
     * @param name the channel name.
     */
    public void recordFloatInput(FloatInput input, String name) {
        input.send(createFloatOutput(name));
    }

    /**
     * Creates a boolean output logging to this recorder.
     *
     * @param name the channel name.
     * @return the new boolean output.
     */
    public BooleanOutput createBooleanOutput(String name) {
        int channel = initChannel(RawType.BOOLEAN, name);
        return (b) -> {
            rec.recordByte(channel, b ? (byte) 1 : (byte) 0);
        };
    }

    /**
     * Records a boolean input.
     *
     * @param input the input to record.
     * @param name the channel name.
     */
    public void recordBooleanInput(BooleanInput input, String name) {
        input.send(createBooleanOutput(name));
    }

    /**
     * Creates a discrete output logging to this recorder.
     *
     * @param name the channel name.
     * @param type the discrete output's type.
     * @return the new discrete output.
     */
    public <E> DiscreteOutput<E> createDiscreteOutput(String name, DiscreteType<E> type) {
        int channel = initChannel(RawType.DISCRETE, name);
        return new DiscreteOutput<E>() {
            @Override
            public DiscreteType<E> getType() {
                return type;
            }

            @Override
            public void set(E e) {
                // TODO: a more space-efficient coding
                rec.recordString(channel, type.toString(e));
            }
        };
    }

    /**
     * Records a discrete input.
     *
     * @param input the input to record.
     * @param name the channel name.
     */
    public <E> void recordDiscreteInput(DiscreteInput<E> input, String name) {
        input.send(createDiscreteOutput(name, input.getType()));
    }

    /**
     * Creates an event output logging to this recorder.
     *
     * @param name the channel name.
     * @return the new event output.
     */
    public EventOutput createEventOutput(String name) {
        int channel = initChannel(RawType.EVENT, name);
        return () -> {
            rec.recordNull(channel);
        };
    }

    /**
     * Records an event input.
     *
     * @param input the input to record.
     * @param name the channel name.
     */
    public void recordEventInput(EventInput input, String name) {
        input.send(createEventOutput(name));
    }

    /**
     * Creates an OutputStream logging to this recorder.
     *
     * @param name the channel name.
     * @return the new OutputStream.
     */
    public OutputStream createOutputStream(String name) {
        int channel = initChannel(RawType.OUTPUT_STREAM, name);
        return new OutputStream() {
            private byte[] b = new byte[1]; // TODO: synchronization?
            private boolean closed = false;

            @Override
            public void write(int b) throws IOException {
                if (closed) {
                    throw new IOException("File closed");
                }
                this.b[0] = (byte) b;
                rec.recordBytes(channel, this.b, 0, 1);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                if (closed) {
                    throw new IOException("File closed");
                }
                rec.recordBytes(channel, b, off, len);
            }

            @Override
            public void close() throws IOException {
                super.close();
                closed = true;
                freeChannel(channel);
            }

            @Override
            public void flush() throws IOException {
                if (closed) {
                    throw new IOException("File closed");
                }
                super.flush();
            }
        };
    }

    /**
     * Records a Faultable.
     *
     * @param faults the faults to record.
     * @param name the channel name.
     */
    public <F> void recordFaultable(Faultable<F> faults, String name) {
        for (F f : faults.getPossibleFaults()) {
            recordBooleanInput(faults.getIsFaulting(f), name + ":" + f.toString());
            recordBooleanInput(faults.getIsFaulting(f), name + ":Sticky" + f.toString());
        }
    }

    /**
     * Records and returns a float input.
     *
     * @param name the channel name.
     * @param input the input to record.
     * @return the same input.
     */
    public FloatInput wrap(String name, FloatInput input) {
        recordFloatInput(input, name);
        return input;
    }

    /**
     * Records and returns a boolean input.
     *
     * @param name the channel name.
     * @param input the input to record.
     * @return the same input.
     */
    public BooleanInput wrap(String name, BooleanInput input) {
        recordBooleanInput(input, name);
        return input;
    }

    /**
     * Records and returns an event input.
     *
     * @param name the channel name.
     * @param input the input to record.
     * @return the same input.
     */
    public EventInput wrap(String name, EventInput input) {
        recordEventInput(input, name);
        return input;
    }

    /**
     * Wraps a float output so that it also records anything written.
     *
     * @param name the channel name.
     * @param output the output to propagate to.
     * @return the output that records and writes through.
     */
    public FloatOutput wrap(String name, FloatOutput output) {
        return output.combine(createFloatOutput(name));
    }

    /**
     * Wraps a boolean output so that it also records anything written.
     *
     * @param name the channel name.
     * @param output the output to propagate to.
     * @return the output that records and writes through.
     */
    public BooleanOutput wrap(String name, BooleanOutput output) {
        return output.combine(createBooleanOutput(name));
    }

    /**
     * Wraps an event output so that it also records anything written.
     *
     * @param name the channel name.
     * @param output the output to propagate to.
     * @return the output that records and writes through.
     */
    public EventOutput wrap(String name, EventOutput output) {
        return output.combine(createEventOutput(name));
    }

    /**
     * Wraps a ControlBindingCreator so that everything bound will also be
     * recorded.
     *
     * @param cname the base name for the channels.
     * @param cbc the original ControlBindingCreator.
     * @return the wrapped ControlBindingCreator.
     */
    public ControlBindingCreator wrap(String cname, ControlBindingCreator cbc) {
        return new ControlBindingCreator() {
            @Override
            public FloatInput addFloat(String name) {
                return wrap(cname + ":" + name, cbc.addFloat(name));
            }

            @Override
            public void addFloat(String name, FloatOutput output) {
                cbc.addFloat(name, wrap(cname + ":" + name, output));
            }

            @Override
            public BooleanInput addBoolean(String name) {
                return wrap(cname + ":" + name, cbc.addBoolean(name));
            }

            @Override
            public void addBoolean(String name, BooleanOutput output) {
                cbc.addBoolean(name, wrap(cname + ":" + name, output));
            }
        };
    }

    /**
     * Records the state of a BehaviorArbitrator. The channel's name will be
     * based on the arbitrator's name.
     *
     * @param behaviors the behaviors to record.
     */
    public void recordBehaviors(BehaviorArbitrator behaviors) {
        recordDiscreteInput(behaviors.getActiveBehavior(), "Behaviors:" + behaviors.getName());
    }

    static int[] listUsedNumbers() {
        String[] files = Storage.list();
        int[] found = new int[files.length];
        int j = 0;
        for (int i = 0; i < files.length; i++) {
            String filename = files[i];
            if (filename.startsWith("rec-")) {
                try {
                    if (filename.endsWith(".gz")) {
                        found[j] = Integer.parseInt(filename.substring(4, filename.length() - 3));
                        j++;
                    } else {
                        found[j] = Integer.parseInt(filename.substring(4));
                        j++;
                    }
                } catch (NumberFormatException ex) {
                    // not the right kind of file; skip forward
                }
            }
        }
        found = Arrays.copyOf(found, j); // compact
        Arrays.sort(found);
        return found;
    }

    static OutputStream openStream(boolean compressed, int maximum_records) throws IOException {
        if (maximum_records < 1) {
            throw new IllegalArgumentException("Must have at least one slot in record buffer!");
        }
        int[] used = listUsedNumbers();
        if (used.length >= maximum_records) {
            int to_remove = 1 + used.length - maximum_records;
            // wipe out old entries in the buffer
            for (int i = 0; i < to_remove; i++) {
                String thisName = "rec-" + used[i];
                if (Storage.exists(thisName)) {
                    Storage.delete(thisName);
                } else {
                    Storage.delete(thisName + ".gz");
                }
            }
        }
        int next_id = used.length == 0 ? 0 : used[used.length - 1] + 1;
        String next_name = "rec-" + next_id;
        if (compressed) {
            Logger.config("Opening recorder output at rec-" + next_name + ".gz (compressed)");
            return new GZIPOutputStream(Storage.openOutput(next_name + ".gz"));
        } else {
            Logger.config("Opening recorder output at rec-" + next_name + " (uncompressed)");
            return Storage.openOutput(next_name);
        }
    }

    /**
     * Opens a recorder from the limited buffer. This will delete old records,
     * and have at most <code>maximum_records</code> recordings at any time.
     *
     * The recorder will be automatically closed when the JVM shuts down.
     *
     * @param compressed if the recording should be compressed
     * @param maximum_recordings the maximum number of recordings
     * @return the opened recorder
     * @throws IOException
     */
    public static Recorder open(boolean compressed, int maximum_recordings) throws IOException {
        OutputStream out = openStream(compressed, maximum_recordings);
        boolean success = false;
        try {
            Recorder rc = new Recorder(out);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    rc.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, "Shutdown-Recorder"));
            success = true;
            return rc;
        } finally {
            if (!success) {
                out.close();
            }
        }
    }
}
