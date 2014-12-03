package ccre.igneous.devices;

import ccre.igneous.Device;
import ccre.igneous.components.SpacingComponent;
import ccre.igneous.components.TextComponent;

public class HeadingDevice extends Device {

    public HeadingDevice(String string) {
        add(new SpacingComponent(30));
        add(new TextComponent(string));
    }
}
