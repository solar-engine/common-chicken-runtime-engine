package ccre.igneous.devices;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import ccre.igneous.Device;

public abstract class LabelledDevice extends Device {

    private final String label;

    public LabelledDevice(String label) {
        this.label = label;
    }

    @Override
    public final void render(Graphics2D g, int width, int height, FontMetrics fontMetrics, int mouseX, int mouseY) {
        super.render(g, width, height, fontMetrics, mouseX, mouseY);
        g.setColor(Color.WHITE);
        g.drawString(label, 20, height / 2 + fontMetrics.getDescent());
        int shift = fontMetrics.stringWidth(label) + 35;
        g.translate(shift, 0);
        labelledRender(g, width - shift, height, fontMetrics, mouseX - shift, mouseY);
        g.translate(-shift, 0);
    }

    protected abstract void labelledRender(Graphics2D g, int i, int height, FontMetrics fontMetrics, int j, int mouseY);
}
