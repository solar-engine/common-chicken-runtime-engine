package ccre.igneous.components;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.concurrency.ConcurrentDispatchArray;
import ccre.igneous.DeviceComponent;

public class ControlBarComponent extends DeviceComponent implements FloatInput {

    private float value = 0.0f;
    private boolean dragging = false;
    private int maxWidth = 0; // zero means no maximum
    private final ConcurrentDispatchArray<FloatOutput> listeners = new ConcurrentDispatchArray<FloatOutput>();

    @Override
    public int render(Graphics2D g, int width, int height, FontMetrics fontMetrics, int mouseX, int mouseY, int lastShift) {
        int startX = lastShift + 5;
        int startY = 5;
        int endX = width - 5;
        int endY = height - 5;
        int barWidth = endX - startX;
        if (maxWidth != 0 && barWidth > maxWidth) {
            barWidth = maxWidth;
            endX = startX + maxWidth;
        }
        int barHeight = endY - startY;
        int originX = startX + barWidth / 2;
        g.setColor(Color.WHITE);
        g.drawRect(startX - 1, startY - 1, barWidth + 1, barHeight + 1);
        g.setColor(Color.RED);
        int actualLimitX = Math.round((barWidth / 2) * Math.min(1, Math.max(-1, value)));
        if (actualLimitX < 0) {
            g.fillRect(originX + actualLimitX, startY, -actualLimitX, barHeight);
        } else {
            g.fillRect(originX, startY, actualLimitX, barHeight);
        }
        hitzone = new Rectangle(startX, startY, endX - startX, endY - startY);
        return endX + 5;
    }
    
    public ControlBarComponent setMaxWidth(int width) {
        this.maxWidth = width;
        return this;
    }

    public void onPress(int x, int y) {
        dragging = true;
        onMouseMove(x, y);
    }

    public void onMouseMove(int x, int y) {
        if (dragging) {
            Rectangle rect = hitzone.getBounds();
            value = 2 * ((x - rect.x) / (float) rect.width - 0.5f);
            repaint();
            for (FloatOutput o : listeners) {
                o.set(value);
            }
        }
    }

    public void onMouseExit(int x, int y) {
        dragging = false;
    }

    public void onRelease(int x, int y) {
        dragging = false;
    }

    public float get() {
        return value;
    }

    public void send(FloatOutput output) {
        listeners.add(output);
    }

    public void unsend(FloatOutput output) {
        listeners.remove(output);
    }
}
