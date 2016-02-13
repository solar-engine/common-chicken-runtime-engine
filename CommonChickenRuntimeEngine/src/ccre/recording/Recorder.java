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

public class Recorder {
    public static enum RawType {
        FLOAT, BOOLEAN, EVENT, OUTPUT_STREAM, DISCRETE
    }

    private final ChanneledRecorder rec;
    private final AtomicInteger next_channel = new AtomicInteger();
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public Recorder(OutputStream stream) throws IOException {
        this.rec = new ChanneledRecorder(stream);
    }

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

    public FloatOutput createFloatOutput(String name) {
        int channel = initChannel(RawType.FLOAT, name);
        return (f) -> {
            rec.recordInt(channel, Float.floatToIntBits(f));
        };
    }

    public void recordFloatInput(FloatInput input, String name) {
        input.send(createFloatOutput(name));
    }

    public BooleanOutput createBooleanOutput(String name) {
        int channel = initChannel(RawType.BOOLEAN, name);
        return (b) -> {
            rec.recordByte(channel, b ? (byte) 1 : (byte) 0);
        };
    }

    public void recordBooleanInput(BooleanInput input, String name) {
        input.send(createBooleanOutput(name));
    }

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

    public EventOutput createEventOutput(String name) {
        int channel = initChannel(RawType.EVENT, name);
        return () -> {
            rec.recordNull(channel);
        };
    }

    public void recordEventInput(EventInput input, String name) {
        input.send(createEventOutput(name));
    }

    public OutputStream recordOutputStream(OutputStream out, String name) {
        int channel = initChannel(RawType.OUTPUT_STREAM, name);
        return new OutputStream() {
            private byte[] b = new byte[1]; // TODO: synchronization?
            private boolean closed = false;

            @Override
            public void write(int b) throws IOException {
                if (closed) {
                    throw new IOException("File closed");
                }
                out.write(b);
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

    public <F> void recordFaultable(Faultable<F> faults, String name) {
        for (F f : faults.getPossibleFaults()) {
            recordBooleanInput(faults.getIsFaulting(f), name + ":" + f.toString());
            recordBooleanInput(faults.getIsFaulting(f), name + ":Sticky" + f.toString());
        }
    }

    public FloatInput wrap(String name, FloatInput input) {
        recordFloatInput(input, name);
        return input;
    }

    public BooleanInput wrap(String name, BooleanInput input) {
        recordBooleanInput(input, name);
        return input;
    }

    public EventInput wrap(String name, EventInput input) {
        recordEventInput(input, name);
        return input;
    }

    public FloatOutput wrap(String name, FloatOutput output) {
        return output.combine(createFloatOutput(name));
    }

    public BooleanOutput wrap(String name, BooleanOutput output) {
        return output.combine(createBooleanOutput(name));
    }

    public EventOutput wrap(String name, EventOutput output) {
        return output.combine(createEventOutput(name));
    }

    public <E> void recordDiscreteInput(DiscreteInput<E> input, String name) {
        input.send(createDiscreteOutput(name, input.getType()));
    }

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

    public void recordBehaviors(BehaviorArbitrator behaviors) {
        recordDiscreteInput(behaviors.getActiveBehavior(), "Behaviors:" + behaviors.getName());
    }

    public static Recorder open(boolean compressed, int circular_buffer_size) throws IOException {
        if (circular_buffer_size < 1) {
            throw new IllegalArgumentException("Must have at least one slot in circular buffer!");
        }
        circular_buffer_size += 1; // because one empty slot
        boolean[] buffer = new boolean[circular_buffer_size];
        int next_empty = -1;
        for (int i = 0; i < circular_buffer_size; i++) {
            buffer[i] = Storage.exists("rec-" + i) || Storage.exists("rec-" + i + ".gz");
            if (!buffer[i] && next_empty == -1) {
                next_empty = i;
            }
        }
        if (next_empty == -1) {
            next_empty = 0;
        }
        int to_delete = (next_empty + circular_buffer_size - 1) % circular_buffer_size;
        if (buffer[to_delete]) {
            Logger.config("Wiping old buffer entry: rec-" + to_delete);
            if (Storage.exists("rec-" + to_delete)) {
                Storage.delete("rec-" + to_delete);
            }
            if (Storage.exists("rec-" + to_delete + ".gz")) {
                Storage.delete("rec-" + to_delete + ".gz");
            }
        }
        OutputStream out;
        if (compressed) {
            out = new GZIPOutputStream(Storage.openOutput("rec-" + next_empty + ".gz"));
        } else {
            out = Storage.openOutput("rec-" + next_empty);
        }
        Logger.config("Opened recorder output at rec-" + next_empty + (compressed ? ".gz (compressed)" : " (uncompressed)"));
        Recorder rc = new Recorder(out);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                rc.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } , "Shutdown-Recorder"));
        return rc;
    }
}
