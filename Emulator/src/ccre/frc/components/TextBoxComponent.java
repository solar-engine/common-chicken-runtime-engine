/*
 * Copyright 2014-2015 Cel Skeggs
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
package ccre.frc.components;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.LinkedList;

import ccre.frc.DeviceComponent;
import ccre.frc.Rendering;

/**
 * A component displaying multiple lines of text. The text can be changed
 * dynamically. The label will have white text by default.
 *
 * @author skeggsc
 */
public class TextBoxComponent extends DeviceComponent {

    private final LinkedList<String> lines = new LinkedList<String>();
    private Color color = Color.WHITE;

    /**
     * Create a new empty TextComponent.
     */
    public TextBoxComponent() {
    }

    /**
     * Change the color of the text to the given color.
     *
     * @param newColor the new color to display the text as.
     */
    public void setColor(Color newColor) {
        color = newColor;
        repaint();
    }

    /**
     * Add the specified line to the text box.
     *
     * @param line the new line to display.
     */
    public void addLine(String line) {
        synchronized (lines) {
            lines.addFirst(line);
        }
        repaint();
    }

    /**
     * Remove all displayed text.
     */
    public void clearLines() {
        synchronized (lines) {
            lines.clear();
        }
        repaint();
    }

    @Override
    public int render(Graphics2D g, int width, int height, FontMetrics fontMetrics, int mouseX, int mouseY, int lastShift) {
        g.setColor(color);
        g.setFont(Rendering.console);
        synchronized (lines) {
            int y = height - g.getFontMetrics().getDescent();
            Iterator<String> iterator = lines.iterator();
            while (iterator.hasNext()) {
                String line = iterator.next();
                g.drawString(line, lastShift, y);
                y -= g.getFontMetrics().getHeight();
                if (y < -g.getFontMetrics().getHeight()) {
                    // get rid of any excess lines
                    iterator.remove();
                    while (iterator.hasNext()) {
                        iterator.next();
                        iterator.remove();
                    }
                    break;
                }
            }
        }
        hitzone = new Rectangle(lastShift, 0, width, height);
        return width;
    }
}
