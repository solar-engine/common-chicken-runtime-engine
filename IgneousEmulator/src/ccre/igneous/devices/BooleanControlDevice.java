package ccre.igneous.devices;

import ccre.channel.BooleanInputPoll;
import ccre.channel.EventInput;
import ccre.channel.EventStatus;
import ccre.igneous.Device;
import ccre.igneous.components.BooleanTextComponent;
import ccre.igneous.components.SpacingComponent;
import ccre.igneous.components.TextComponent;

public class BooleanControlDevice extends Device implements BooleanInputPoll {

    private final EventStatus pressEvent = new EventStatus();
    private final BooleanTextComponent actuated = new BooleanTextComponent("INACTIVE", "ACTIVE") {
        public void onPress(int x, int y) {
            boolean wasDown = get();
            super.onPress(x, y);
            if (!wasDown && get()) {
                pressEvent.produce();
                repaint();
            }
        }
    }.setEditable(true);

    public BooleanControlDevice(String label) {
        add(new SpacingComponent(20));
        add(new TextComponent(label));
        add(actuated);
    }

    public boolean get() {
        return actuated.get();
    }

    public EventInput whenPressed() {
        return pressEvent;
    }
}
