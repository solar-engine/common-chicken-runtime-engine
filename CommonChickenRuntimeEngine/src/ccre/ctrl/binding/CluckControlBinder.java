/*
 * Copyright 2015 Colby Skeggs
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
package ccre.ctrl.binding;

import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.cluck.Cluck;
import ccre.log.Logger;
import ccre.rconf.RConf;
import ccre.rconf.RConf.Entry;
import ccre.rconf.RConfable;
import ccre.saver.StorageProvider;
import ccre.saver.StorageSegment;
import ccre.util.CHashMap;

public class CluckControlBinder implements RConfable {
    private final ControlBindingDataSource sourceSet;
    private final ControlBindingDataSink sinkSet;
    // From sink to source.
    private final CHashMap<String, String> boolLinkage = new CHashMap<String, String>();
    private final CHashMap<String, String> floatLinkage = new CHashMap<String, String>();
    private final String title;
    private boolean dirty = false;
    private final StorageSegment storage;

    public CluckControlBinder(String title, ControlBindingDataSource source, ControlBindingDataSink sink) {
        this.title = title;
        this.sourceSet = source;
        this.sinkSet = sink;
        storage = StorageProvider.openStorage("Control Bindings: " + title);
        if (sink.listBooleans().length != 0 || sink.listFloats().length != 0) {
            load();
        }
    }

    public static ControlBindingCreator makeCreator(String title, ControlBindingDataSource source, EventInput load) {
        ControlBindingDataSinkBuildable sink = new ControlBindingDataSinkBuildable();
        final CluckControlBinder binder = new CluckControlBinder(title, source, sink);
        binder.publish();
        if (load == null) {
            throw new IllegalArgumentException("makeCreator expects a 'load' event because, otherwise, it doesn't actually know when to load the settings!");
        } else {
            load.send(new EventOutput() {
                public void event() {
                    binder.load();
                }
            });
        }
        return sink;
    }

    public void publish() {
        publish(title + " Control Bindings");
    }

    public void publish(String name) {
        Cluck.publishRConf(name, this);
    }

    public Entry[] queryRConf() throws InterruptedException {
        String[] boolSinks = sinkSet.listBooleans();
        String[] floatSinks = sinkSet.listFloats();
        Entry[] ents = new Entry[floatSinks.length + boolSinks.length + 6 + (floatSinks.length == 0 ? 0 : 1) + (boolSinks.length == 0 ? 0 : 1)];
        ents[0] = RConf.title(title);
        ents[1] = RConf.string("Click a binding while holding the new button or axis");
        ents[2] = RConf.string("Click without holding anything to clear");
        ents[3] = dirty ? RConf.button("Save Configuration") : RConf.string("Save Configuration");
        ents[4] = dirty ? RConf.button("Load Configuration") : RConf.string("Load Configuration");
        ents[5] = RConf.autoRefresh(10000);
        int n = 6;
        if (boolSinks.length != 0) {
            ents[n++] = RConf.title("Buttons:");
            for (String sink : boolSinks) {
                String source = boolLinkage.get(sink);
                ents[n++] = RConf.button(sink + ": " + (source == null ? "unbound" : source));
            }
        }
        if (floatSinks.length != 0) {
            ents[n++] = RConf.title("Axes:");
            for (String sink : floatSinks) {
                String source = floatLinkage.get(sink);
                ents[n++] = RConf.button(sink + ": " + (source == null ? "unbound" : source));
            }
        }
        if (ents.length != n) {
            throw new RuntimeException("Oops! Mismatch of RConf array length in CluckControlBinder.");
        }
        return ents;
    }

    public boolean signalRConf(int field, byte[] data) throws InterruptedException {
        if (field == 3 && dirty) {
            save();
            return true;
        }
        if (field == 4 && dirty) {
            load();
            return true;
        }
        String[] boolSinks = sinkSet.listBooleans();
        String[] floatSinks = sinkSet.listFloats();
        int n = 6;
        if (boolSinks.length != 0) {
            n++;
            for (String sink : boolSinks) {
                if (field == n++) {
                    String source = getActiveBoolSource();
                    rebindBoolean(sink, source);
                    dirty = true;
                    return true;
                }
            }
        }
        if (floatSinks.length != 0) {
            n++;
            for (String sink : floatSinks) {
                if (field == n++) {
                    String source = getActiveFloatSource();
                    rebindFloat(sink, source);
                    dirty = true;
                    return true;
                }
            }
        }
        return false;
    }

    private void rebindBoolean(String sink, String source) {
        String oldSource = boolLinkage.get(sink);
        if (oldSource != null) {
            sourceSet.getBoolean(oldSource).unsend(sinkSet.getBoolean(sink));
        }

        if (source == null) {
            boolLinkage.remove(sink);
        } else {
            sourceSet.getBoolean(source).send(sinkSet.getBoolean(sink));
            boolLinkage.put(sink, source);
        }
    }

    private void rebindFloat(String sink, String source) {
        String oldSource = floatLinkage.get(sink);
        if (oldSource != null) {
            sourceSet.getFloat(oldSource).unsend(sinkSet.getFloat(sink));
        }

        if (source == null) {
            floatLinkage.remove(sink);
        } else {
            sourceSet.getFloat(source).send(sinkSet.getFloat(sink));
            floatLinkage.put(sink, source);
        }
    }

    private String getActiveBoolSource() {
        String found = null;
        for (String bin : sourceSet.listBooleans()) {
            if (sourceSet.getBoolean(bin).get()) {
                if (found != null) {
                    Logger.warning("More than one active boolean source is pressed: at least '" + found + "' and '" + bin + "'");
                    return null;
                }
                found = bin;
            }
        }
        return found;
    }

    private String getActiveFloatSource() {
        String found = null;
        for (String fin : sourceSet.listFloats()) {
            if (Math.abs(sourceSet.getFloat(fin).get()) >= 0.8f) {
                if (found != null) {
                    Logger.warning("More than one active float source is pressed: at least '" + found + "' and '" + fin + "'");
                    return null;
                }
                found = fin;
            }
        }
        return found;
    }

    private void load() {
        Logger.config("Loading control bindings for " + this.title);
        for (String boolSink : sinkSet.listBooleans()) {
            String source = storage.getStringForKey("z" + boolSink);
            if (source != null && sourceSet.getBoolean(source) == null) {
                Logger.warning("Invalid control binding boolean source: " + source);
            } else {
                rebindBoolean(boolSink, source);
            }
        }
        for (String floatSink : sinkSet.listFloats()) {
            String source = storage.getStringForKey("f" + floatSink);
            if (source != null && sourceSet.getFloat(source) == null) {
                Logger.warning("Invalid control binding float source: " + source);
            } else {
                rebindFloat(floatSink, source);
            }
        }
        Logger.config("Loaded " + (boolLinkage.size() + floatLinkage.size()) + " of " + (sinkSet.listBooleans().length + sinkSet.listFloats().length) + " control bindings for " + this.title);
        dirty = false;
    }

    private void save() {
        for (String boolSink : sinkSet.listBooleans()) {
            storage.setStringForKey("z" + boolSink, boolLinkage.get(boolSink));
        }
        for (String floatSink : sinkSet.listFloats()) {
            storage.setStringForKey("f" + floatSink, floatLinkage.get(floatSink));
        }
        storage.flush();
        dirty = false;
    }
}
