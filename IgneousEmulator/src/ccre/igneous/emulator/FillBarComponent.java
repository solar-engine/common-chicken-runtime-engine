package ccre.igneous.emulator;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import ccre.igneous.emulator.DeviceComponent;

public class FillBarComponent extends DeviceComponent {
    
    private float value = 0.0f;

    @Override
    public int render(Graphics2D g, int width, int height, FontMetrics fontMetrics, int mouseX, int mouseY, int lastShift) {
        int startX = lastShift + 5;
        int startY = 5;
        int endX = width - 5;
        int endY = height - 5;
        int barWidth = endX - startX;
        int barHeight = endY - startY;
        int originX = startX + barWidth / 2;
        g.setColor(Color.WHITE);
        g.drawRect(startX - 1, startY - 1, barWidth + 1, barHeight + 1);
        g.setColor(Color.CYAN);
        int actualLimitX = Math.round((barWidth / 2) * Math.min(1, Math.max(-1, value)));
        if (actualLimitX < 0) {
            g.fillRect(originX + actualLimitX, startY, -actualLimitX, barHeight);
        } else {
            g.fillRect(originX, startY, actualLimitX, barHeight);
        }
        return width;
    }

    public void set(float value) {
        this.value = value;
    }
}
