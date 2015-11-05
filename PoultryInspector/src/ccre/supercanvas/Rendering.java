/*
 * Copyright 2014-2015 Colby Skeggs
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
package ccre.supercanvas;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

/**
 * Some common rendering utilities for the Poultry Inspector.
 *
 * @author skeggsc
 */
public class Rendering {
    /**
     * Console text, small and monospaced.
     */
    public static final Font console = new Font("Monospaced", Font.PLAIN, 11);
    /**
     * Mid-label text, medium and monospaced.
     */
    public static final Font midlabels = new Font("Monospaced", Font.BOLD, 20);
    /**
     * Label text, large-ish and monospaced.
     */
    public static final Font labels = new Font("Monospaced", Font.BOLD, 30);

    /**
     * Render the main body of a standard component.
     *
     * @param bg the background color.
     * @param g the graphics to draw on.
     * @param centerX the center X position.
     * @param centerY the center Y position.
     * @param width the width of the box.
     * @param height the height of the box.
     */
    public static void drawBody(Color bg, Graphics2D g, int centerX, int centerY, int width, int height) {
        g.setColor(bg);
        Shape s = new Rectangle2D.Float(centerX - width / 2, centerY - height / 2, width, height);
        g.fill(s);
        g.setColor(Color.BLACK);
        g.draw(s);
    }

    /**
     * A convenience function to render a DraggableBoxComponent as if drawBody
     * had been called.
     *
     * @param bg the background color.
     * @param g the graphics to draw on.
     * @param component the component to render
     * @see #drawBody(Color, Graphics2D, int, int, int, int)
     */
    public static void drawBody(Color bg, Graphics2D g, DraggableBoxComponent component) {
        drawBody(bg, g, component.centerX, component.centerY, component.halfWidth * 2, component.halfHeight * 2);
    }

    /**
     * Draw a scrollbar nub.
     *
     * @param g the graphics to draw on.
     * @param active if the scrollbar is "active" (away from the starting
     * location)
     * @param x the center X position.
     * @param y the center Y position.
     */
    public static void drawScrollbar(Graphics2D g, boolean active, int x, int y) {
        g.setColor(active ? Color.BLUE : Color.GREEN);
        Shape s = new Rectangle(x - 3, y - 3, 6, 6);
        g.fill(s);
        g.setColor(Color.BLACK);
        g.draw(s);
    }

    /**
     * Blend the specified colors using the specified fraction. 0 means all
     * color A, 1 means all color B, 0.5 is half-and-half, etc.
     *
     * @param a The first color.
     * @param b The second color.
     * @param f The blending factor.
     * @return The blended color.
     */
    public static Color blend(Color a, Color b, float f) {
        float bpart;
        if (f < 0) {
            bpart = 0;
        } else if (f > 1) {
            bpart = 1;
        } else {
            bpart = f;
        }
        float apart = 1 - bpart;
        return new Color(Math.round(a.getRed() * apart + b.getRed() * bpart), Math.round(a.getGreen() * apart + b.getGreen() * bpart), Math.round(a.getBlue() * apart + b.getBlue() * bpart), Math.round(a.getAlpha() * apart + b.getAlpha() * bpart));
    }
}
