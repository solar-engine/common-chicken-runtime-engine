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
package ccre.supercanvas.components;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import ccre.supercanvas.DraggableBoxComponent;
import ccre.supercanvas.Rendering;

/**
 * A simple component that displays an editable label.
 *
 * @author skeggsc
 */
public class TextComponent extends DraggableBoxComponent {

    private static final long serialVersionUID = -4289225098349876909L;
    private final StringBuilder contents;

    /**
     * Create a new TextComponent with specified default text.
     *
     * @param cx The X-coordinate.
     * @param cy The Y-coordinate.
     * @param default_ The default text contents.
     */
    public TextComponent(int cx, int cy, String default_) {
        super(cx, cy);
        contents = new StringBuilder(default_);
    }

    /**
     * Create a new TextComponent.
     *
     * @param cx The X-coordinate.
     * @param cy The Y-coordinate.
     */
    public TextComponent(int cx, int cy) {
        this(cx, cy, "");
    }

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        g.setFont(Rendering.labels);
        String render = contents.toString();
        boolean grey = false;
        if (render.isEmpty()) {
            render = "...";
            grey = true;
        }
        this.halfWidth = 5 + g.getFontMetrics().stringWidth(render) / 2;
        this.halfHeight = g.getFontMetrics().getHeight() / 2;
        if (getPanel().editing == contents || getPanel().editmode) {
            Rendering.drawBody(getPanel().editing == contents ? Color.GREEN : Color.YELLOW, g, this);
        }
        g.setColor(grey ? Color.GRAY : Color.BLACK);
        g.drawString(render, centerX - halfWidth + 5, centerY + 5);
    }

    @Override
    public void onPressedEnter() {
        if (getPanel().editing == contents) {
            getPanel().editing = null;
        }
    }

    @Override
    public boolean onInteract(int x, int y) {
        if (getPanel().editing == contents) {
            getPanel().editing = null;
        } else {
            getPanel().editing = contents;
        }
        return true;
    }

    public String toString() {
        return "Text Component [" + contents + "]";
    }
}
