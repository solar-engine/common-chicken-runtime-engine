package ccre.igneous.emulator.devices;

import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.igneous.emulator.Device;
import ccre.igneous.emulator.components.ControlBarComponent;
import ccre.igneous.emulator.components.SpacingComponent;
import ccre.igneous.emulator.components.TextComponent;

public class FloatControlDevice extends Device implements FloatInput {

    private final ControlBarComponent value = new ControlBarComponent();

    public FloatControlDevice(String label) {
        add(new SpacingComponent(20));
        add(new TextComponent(label));
        add(value);
    }

    public float get() {
        return value.get();
    }

    public void send(FloatOutput output) {
        value.send(output);
    }

    public void unsend(FloatOutput output) {
        value.unsend(output);
    }
}
