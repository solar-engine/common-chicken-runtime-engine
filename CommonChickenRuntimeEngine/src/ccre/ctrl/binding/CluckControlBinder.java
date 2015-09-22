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

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.cluck.Cluck;
import ccre.rconf.RConf;
import ccre.rconf.RConf.Entry;
import ccre.rconf.RConfable;
import ccre.storage.Storage;
import ccre.storage.StorageSegment;

/**
 * A CluckControlBinder connects together a ControlBindingDataSource (such as a
 * set of Joysticks) to a ControlBindingDataSink (such as your program's
 * controls.) It allows for configuration of the linkage over Cluck and allows
 * for saving the control binding configuration.
 *
 * @author skeggsc
 */
public class CluckControlBinder implements RConfable {
    private static Logger logger = LoggerFactory.getLogger(CluckControlBinder.class);

    private final ControlBindingDataSource sourceSet;
    private final ControlBindingDataSink sinkSet;
    // From sink to source.
    private final HashMap<String, String> boolLinkage = new HashMap<String, String>();
    private final HashMap<String, String> floatLinkage = new HashMap<String, String>();

    private final HashMap<String, EventOutput> boolUnbinds = new HashMap<String, EventOutput>();
    private final HashMap<String, EventOutput> floatUnbinds = new HashMap<String, EventOutput>();

    private final String name;
    private boolean dirty = false;
    private final StorageSegment storage;

    /**
     * Create a new CluckControlBinder, published with the specified name, that
     * binds together the provided source and sink.
     *
     * If anything is already available on the sink, this will also attempt to
     * load the saved configuration.
     *
     * @param name the name of this binding, used for the StorageSegment name
     * and the Cluck link.
     * @param source the data source to bind.
     * @param sink the data sink to bind.
     */
    public CluckControlBinder(String name, ControlBindingDataSource source, ControlBindingDataSink sink) {
        this.name = name;
        this.sourceSet = source;
        this.sinkSet = sink;
        storage = Storage.openStorage("Control Bindings: " + name);
        if (sink.listBooleans().length != 0 || sink.listFloats().length != 0) {
            load();
        }
    }

    /**
     * Provide a ControlBindingCreator for the given source, which is
     * configurable over Cluck under the specified name.
     *
     * This will load any saved configuration when the load event is produced.
     * This is recommended to be produced exactly once, at the end of
     * initialization. The provided RConf interface includes buttons for saving
     * and loading.
     *
     * @param name the name for the CluckControlBinder created as part of this.
     * @param source the data source that controls can be assigned from.
     * @param load when to load the configuration for this CluckControlBinder.
     * @return the ControlBindingCreator that a program can use to provide its
     * controls that it wants bound.
     */
    public static ControlBindingCreator makeCreator(String name, ControlBindingDataSource source, EventInput load) {
        ControlBindingDataSinkBuildable sink = new ControlBindingDataSinkBuildable();
        final CluckControlBinder binder = new CluckControlBinder(name, source, sink);
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

    /**
     * Publish the RConf interface for this binder under the name
     * "[NAME] Control Bindings".
     *
     * For example, if the name of this CluckControlBinder were "Drive Code",
     * the RConf interface would be available under
     * "Drive Code Control Bindings".
     *
     * This is equivalent to <code>publish(name + " Control Bindings");</code>
     *
     * @see #publish(String)
     */
    public void publish() {
        publish(name + " Control Bindings");
    }

    /**
     * Publish the RConf interface for this binder under the specified link
     * name.
     *
     * The RConf interface includes saving and loading buttons, along with
     * buttons to bind any of the control sinks to the currently activated
     * control source.
     *
     * See the published RConf interface for more details.
     *
     * @param name the link name for this RConf interface.
     */
    public void publish(String name) {
        Cluck.publishRConf(name, this);
    }

    public Entry[] queryRConf() throws InterruptedException {
        String[] boolSinks = sinkSet.listBooleans();
        String[] floatSinks = sinkSet.listFloats();
        Entry[] ents = new Entry[floatSinks.length + boolSinks.length + 6 + (floatSinks.length == 0 ? 0 : 1) + (boolSinks.length == 0 ? 0 : 1)];
        ents[0] = RConf.title(name);
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
        EventOutput unbind = boolUnbinds.get(sink);
        if (unbind != null) {
            unbind.event();
        }

        if (source == null) {
            boolLinkage.remove(sink);
            boolUnbinds.remove(sink);
        } else {
            unbind = sourceSet.getBoolean(source).sendR(sinkSet.getBoolean(sink));
            boolLinkage.put(sink, source);
            boolUnbinds.put(sink, unbind);
        }
    }

    private void rebindFloat(String sink, String source) {
        EventOutput unbind = floatUnbinds.get(sink);
        if (unbind != null) {
            unbind.event();
        }

        if (source == null) {
            floatLinkage.remove(sink);
            floatUnbinds.remove(sink);
        } else {
            unbind = sourceSet.getFloat(source).sendR(sinkSet.getFloat(sink));
            floatLinkage.put(sink, source);
            floatUnbinds.put(sink, unbind);
        }
    }

    private String getActiveBoolSource() {
        String found = null;
        for (String bin : sourceSet.listBooleans()) {
            if (sourceSet.getBoolean(bin).get()) {
                if (found != null) {
                    logger.warn("More than one active boolean source is pressed: at least '" + found + "' and '" + bin + "'");
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
                    logger.warn("More than one active float source is pressed: at least '" + found + "' and '" + fin + "'");
                    return null;
                }
                found = fin;
            }
        }
        return found;
    }

    private void load() {
        logger.debug("Loading control bindings for " + this.name);
        for (String boolSink : sinkSet.listBooleans()) {
            String source = storage.getStringForKey("z" + boolSink);
            if (source != null && sourceSet.getBoolean(source) == null) {
                logger.warn("Invalid control binding boolean source: " + source);
            } else {
                rebindBoolean(boolSink, source);
            }
        }
        for (String floatSink : sinkSet.listFloats()) {
            String source = storage.getStringForKey("f" + floatSink);
            if (source != null && sourceSet.getFloat(source) == null) {
                logger.warn("Invalid control binding float source: " + source);
            } else {
                rebindFloat(floatSink, source);
            }
        }
        logger.debug("Loaded " + (boolLinkage.size() + floatLinkage.size()) + " of " + (sinkSet.listBooleans().length + sinkSet.listFloats().length) + " control bindings for " + this.name);
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
