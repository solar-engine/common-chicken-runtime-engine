package ccre.ctrl.binding;

import ccre.channel.BooleanOutput;
import ccre.channel.FloatOutput;

public interface ControlBindingDataSink {
    public String[] listBooleans();

    public BooleanOutput getBoolean(String name);

    public String[] listFloats();

    public FloatOutput getFloat(String name);
}
