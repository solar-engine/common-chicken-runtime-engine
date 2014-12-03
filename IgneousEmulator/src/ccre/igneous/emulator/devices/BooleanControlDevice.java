package ccre.igneous.emulator.devices;

import ccre.channel.BooleanInputPoll;
import ccre.igneous.emulator.Device;
import ccre.igneous.emulator.components.BooleanTextComponent;
import ccre.igneous.emulator.components.SpacingComponent;
import ccre.igneous.emulator.components.TextComponent;

public class BooleanControlDevice extends Device implements BooleanInputPoll {

    private final BooleanTextComponent actuated = new BooleanTextComponent("DEACTIVE", "ACTIVE").setEditable(true);

    public BooleanControlDevice(String label) {
        add(new SpacingComponent(20));
        add(new TextComponent(label));
        add(actuated);
    }

    public boolean get() {
        return actuated.get();
    }
}
