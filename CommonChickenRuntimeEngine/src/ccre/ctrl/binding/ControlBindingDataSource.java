package ccre.ctrl.binding;

import ccre.channel.BooleanInput;
import ccre.channel.FloatInput;

public interface ControlBindingDataSource {
    public String[] listBooleans();

    public BooleanInput getBoolean(String name);

    public String[] listFloats();

    public FloatInput getFloat(String name);
}
