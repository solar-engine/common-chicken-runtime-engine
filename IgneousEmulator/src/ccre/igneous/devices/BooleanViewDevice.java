package ccre.igneous.devices;

import ccre.channel.BooleanOutput;
import ccre.igneous.Device;
import ccre.igneous.components.BooleanTextComponent;
import ccre.igneous.components.SpacingComponent;
import ccre.igneous.components.TextComponent;

public class BooleanViewDevice extends Device implements BooleanOutput {

    private final BooleanTextComponent actuated = new BooleanTextComponent("DEACTUATED", "ACTUATED");

    public BooleanViewDevice(String label) {
        add(new SpacingComponent(20));
        add(new TextComponent(label));
        add(actuated);
    }

    public void set(boolean value) {
        actuated.set(value);
    }

    public boolean get() {
        return actuated.get();
    }
}
