package ccre.igneous.emulator.devices;

import ccre.channel.FloatOutput;
import ccre.igneous.emulator.Device;
import ccre.igneous.emulator.components.FillBarComponent;
import ccre.igneous.emulator.components.SpacingComponent;
import ccre.igneous.emulator.components.TextComponent;

public class FloatViewDevice extends Device implements FloatOutput {

    private final FillBarComponent value = new FillBarComponent();
    private final float minInput, maxInput;

    public FloatViewDevice(String label) {
        this(label, -1, 1);
    }

    public FloatViewDevice(String label, float minInput, float maxInput) {
        add(new SpacingComponent(20));
        add(new TextComponent(label));
        add(value);
        this.minInput = minInput;
        this.maxInput = maxInput;
    }

    public void set(float value) {
        this.value.set(2 * (value - minInput) / (maxInput - minInput) - 1);
    }
}
