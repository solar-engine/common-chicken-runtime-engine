package ccre.ctrl.binding;

import ccre.cluck.Cluck;
import ccre.log.Logger;
import ccre.rconf.RConf;
import ccre.rconf.RConf.Entry;
import ccre.rconf.RConfable;
import ccre.util.CHashMap;

public class CluckControlBinder implements RConfable {
    private final ControlBindingDataSource sourceSet;
    private final ControlBindingDataSink sinkSet;
    // From sink to source.
    private final CHashMap<String, String> boolLinkage = new CHashMap<String, String>();
    private final CHashMap<String, String> floatLinkage = new CHashMap<String, String>();
    private final String title;

    public CluckControlBinder(String title, ControlBindingDataSource source, ControlBindingDataSink sink) {
        this.title = title;
        this.sourceSet = source;
        this.sinkSet = sink;
    }
    
    public static ControlBindingCreator makeCreator(String title, ControlBindingDataSource source) {
        ControlBindingDataSinkBuildable sink = new ControlBindingDataSinkBuildable();
        new CluckControlBinder(title, source, sink).publish();
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
        Entry[] ents = new Entry[floatSinks.length + boolSinks.length + 4 + (floatSinks.length == 0 ? 0 : 1) + (boolSinks.length == 0 ? 0 : 1)];
        ents[0] = RConf.title(title);
        ents[1] = RConf.string("Click a binding while holding the new button or axis");
        ents[2] = RConf.string("Click without holding anything to clear");
        ents[3] = RConf.autoRefresh(10000);
        int n = 4;
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
        String[] boolSinks = sinkSet.listBooleans();
        String[] floatSinks = sinkSet.listFloats();
        int n = 4;
        if (boolSinks.length != 0) {
            n++;
            for (String sink : boolSinks) {
                if (field == n++) {
                    String oldSource = boolLinkage.get(sink);
                    if (oldSource != null) {
                        sourceSet.getBoolean(oldSource).unsend(sinkSet.getBoolean(sink));
                    }
                    
                    String source = getActiveBoolSource();
                    if (source == null) {
                        boolLinkage.remove(sink);
                    } else {
                        sourceSet.getBoolean(source).send(sinkSet.getBoolean(sink));
                        boolLinkage.put(sink, source);
                    }
                    return true;
                }
            }
        }
        if (floatSinks.length != 0) {
            n++;
            for (String sink : floatSinks) {
                if (field == n++) {
                    String oldSource = floatLinkage.get(sink);
                    if (oldSource != null) {
                        sourceSet.getFloat(oldSource).unsend(sinkSet.getFloat(sink));
                    }
                    
                    String source = getActiveFloatSource();
                    if (source == null) {
                        floatLinkage.remove(sink);
                    } else {
                        sourceSet.getFloat(source).send(sinkSet.getFloat(sink));
                        floatLinkage.put(sink, source);
                    }
                    return true;
                }
            }
        }
        return false;
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
}
