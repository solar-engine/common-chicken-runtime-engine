/*
 * Copyright 2014 Colby Skeggs
 * 
 * This file is part of the CCRE, the Common Chicken Runtime Engine.
 * 
 * The CCRE is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * The CCRE is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the CCRE.  If not, see <http://www.gnu.org/licenses/>.
 */
package ccre.igneous.components;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import ccre.igneous.DeviceComponent;

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
