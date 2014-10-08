/*
 * Copyright 2014 Colby Skeggs.
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
package supercanvas;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * A component that represents an object that can't otherwise be displayed.
 *
 * @author skeggsc
 */
public class FictionalComponent extends DraggableBoxComponent {

    private static final long serialVersionUID = -8887913075410335155L;
    private final String name;
    private final String tstr;

    /**
     * Create a new FictionalComponent.
     *
     * @param cx the X-coordinate.
     * @param cy the Y-coordinate.
     * @param name the name of the FictionalComponent.
     * @param tstring The string representing the type of object that can't be
     * displayed.
     */
    public FictionalComponent(int cx, int cy, String name, String tstring) {
        super(cx, cy);
        this.name = name;
        this.tstr = tstring;
    }

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        halfWidth = Math.max(30, Math.max(fontMetrics.stringWidth(name) / 2, fontMetrics.stringWidth(tstr) / 2)) + 5;
        halfHeight = fontMetrics.getHeight() + 1;
        Rendering.drawBody(Color.GRAY, g, this);
        g.setColor(Color.BLACK);
        g.drawString(name, centerX - halfWidth + 5, centerY - halfHeight + 1 + fontMetrics.getAscent());
        g.drawString(tstr, centerX - halfWidth + 5, centerY - halfHeight + 1 + fontMetrics.getAscent() + fontMetrics.getHeight());
    }

    @Override
    public boolean onInteract(int x, int y) {
        return false;
    }

    @Override
    public String toString() {
        return "Fictional Component: " + name + "[" + tstr + "]";
    }
}
