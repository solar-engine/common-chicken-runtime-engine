package ccre.ctrl.binding;

import java.util.ConcurrentModificationException;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.BooleanStatus;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.channel.FloatStatus;
import ccre.util.CArrayList;
import ccre.util.CArrayUtils;
import ccre.util.CHashMap;

public class ControlBindingDataSinkBuildable implements ControlBindingDataSink, ControlBindingCreator {

    private final CHashMap<String, BooleanOutput> booleans = new CHashMap<String, BooleanOutput>();
    private final CHashMap<String, FloatOutput> floats = new CHashMap<String, FloatOutput>();

    public String[] listBooleans() {
        String[] stra;
        CArrayList<String> strs = CArrayUtils.collectIterable(booleans);
        stra = new String[strs.size()];
        if (strs.fillArray(stra) != 0) {
            throw new ConcurrentModificationException();
        }
        return stra;
    }

    public BooleanOutput getBoolean(String name) {
        return booleans.get(name);
    }

    public String[] listFloats() {
        String[] stra;
        CArrayList<String> strs = CArrayUtils.collectIterable(floats);
        stra = new String[strs.size()];
        if (strs.fillArray(stra) != 0) {
            throw new ConcurrentModificationException();
        }
        return stra;
    }

    public FloatOutput getFloat(String name) {
        return floats.get(name);
    }

    public void addBoolean(String name, BooleanOutput output) {
        if (booleans.containsKey(name)) {
            throw new IllegalArgumentException("Boolean sink already registered: '" + name + "'");
        }
        booleans.put(name, output);
    }

    public BooleanInput addBoolean(String name) {
        BooleanStatus status = new BooleanStatus();
        addBoolean(name, status.asOutput());
        return status.asInput();
    }

    public void addFloat(String name, FloatOutput output) {
        if (floats.containsKey(name)) {
            throw new IllegalArgumentException("Float sink already registered: '" + name + "'");
        }
        floats.put(name, output);
    }

    public FloatInput addFloat(String name) {
        FloatStatus status = new FloatStatus();
        addFloat(name, status.asOutput());
        return status.asInput();
    }
}
