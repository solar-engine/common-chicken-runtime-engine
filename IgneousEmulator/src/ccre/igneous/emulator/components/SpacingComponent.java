package ccre.igneous.emulator.components;

import java.awt.FontMetrics;
import java.awt.Graphics2D;

import ccre.igneous.emulator.DeviceComponent;

public class SpacingComponent extends DeviceComponent {

    private final int spacing;
    
    public SpacingComponent(int spacing) {
        this.spacing = spacing;
    }

    @Override
    public int render(Graphics2D g, int width, int height, FontMetrics fontMetrics, int mouseX, int mouseY, int lastShift) {
        return lastShift + spacing;
    }

}
