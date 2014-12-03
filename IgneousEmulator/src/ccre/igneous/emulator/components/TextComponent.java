package ccre.igneous.emulator.components;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import ccre.igneous.emulator.DeviceComponent;

public class TextComponent extends DeviceComponent {
    
    private String label;
    private final String[] widthcalc;
    private Color color = Color.WHITE;

    public TextComponent(String label) {
        this.label = label;
        widthcalc = new String[] {label};
    }

    public TextComponent(String label, String[] widthcalc) {
        this.label = label;
        this.widthcalc = widthcalc;
    }
    
    public void setColor(Color newColor) {
        color = newColor;
        repaint();
    }
    
    public void setLabel(String newLabel) {
        label = newLabel;
        repaint();
    }

    @Override
    public int render(Graphics2D g, int width, int height, FontMetrics fontMetrics, int mouseX, int mouseY, int lastShift) {
        g.setColor(color);
        int maxWidth = fontMetrics.stringWidth(label);
        for (String str : widthcalc) {
            maxWidth = Math.max(maxWidth, fontMetrics.stringWidth(str));
        }
        g.drawString(label, lastShift + maxWidth / 2 - fontMetrics.stringWidth(label) / 2, height / 2 + fontMetrics.getDescent());
        hitzone = new Rectangle(lastShift, 0, maxWidth, height);
        return lastShift + maxWidth + 15;
    }
}
