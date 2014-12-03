package ccre.igneous.devices;

import ccre.igneous.DeviceGroup;

public class DSLCDDevice extends DeviceGroup {

    private TextualDisplayDevice[] lines = new TextualDisplayDevice[6];

    public DSLCDDevice() {
        add(new HeadingDevice("Driver Station LCD"));
        for (int i = 0; i < lines.length; i++) {
            add(lines[i] = new TextualDisplayDevice("....................", 30));
        }
    }

    public void update(int lineid, String value) {
        if (lineid < 1 || lineid > lines.length) {
            throw new IllegalArgumentException("Invalid DS LCD line: " + lineid);
        }
        lines[lineid - 1].set(value);
    }
}
