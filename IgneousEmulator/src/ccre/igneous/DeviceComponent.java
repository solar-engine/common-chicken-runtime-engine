package ccre.igneous;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;

public abstract class DeviceComponent {

    private Device parent;
    protected Shape hitzone;

    void setDevice(Device parent) {
        this.parent = parent;
    }

    protected void repaint() {
        if (parent != null) {
            parent.repaint();
        }
    }

    public abstract int render(Graphics2D g, int width, int height, FontMetrics fontMetrics, int mouseX, int mouseY, int lastShift);

    public void checkPress(int x, int y) {
        if (hitzone != null && hitzone.contains(x, y)) {
            onPress(x, y);
        }
    }

    protected void onPress(int x, int y) {
        // Do nothing by default.
    }

    public void onMouseMove(int x, int y) {
        // Do nothing by default.
    }

    public void onMouseEnter(int x, int y) {
        // Do nothing by default.
    }

    public void onMouseExit(int x, int y) {
        // Do nothing by default.
    }

    public void onRelease(int x, int y) {
        // Do nothing by default.
    }
}
