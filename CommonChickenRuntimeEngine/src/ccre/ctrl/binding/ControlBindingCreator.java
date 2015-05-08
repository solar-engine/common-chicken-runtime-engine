package ccre.ctrl.binding;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;

public interface ControlBindingCreator {
    public void addBoolean(String name, BooleanOutput output);

    public BooleanInput addBoolean(String name);

    public void addFloat(String name, FloatOutput output);

    public FloatInput addFloat(String name);
}
