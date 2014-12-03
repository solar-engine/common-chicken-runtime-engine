package ccre.igneous.devices;

import ccre.igneous.Device;
import ccre.igneous.components.SpacingComponent;
import ccre.igneous.components.TextComponent;

public class TextualDisplayDevice extends Device {

    private final int height;
    private final TextComponent text;

    public TextualDisplayDevice(String string, int height) {
        this.height = height;
        add(new SpacingComponent(20));
        text = new TextComponent(string);
        add(text);
    }
    
    public void set(String str) {
        text.setLabel(str);
    }
    
    public int getHeight() {
        return height;
    }
}