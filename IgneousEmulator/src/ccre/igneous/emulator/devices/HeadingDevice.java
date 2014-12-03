package ccre.igneous.emulator.devices;

import ccre.igneous.emulator.Device;
import ccre.igneous.emulator.components.SpacingComponent;
import ccre.igneous.emulator.components.TextComponent;

public class HeadingDevice extends Device {

    public HeadingDevice(String string) {
        add(new SpacingComponent(30));
        add(new TextComponent(string));
    }
}
