/*
 * Copyright 2015 Colby Skeggs
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
package ccre.supercanvas.components.channels;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import ccre.log.LogLevel;
import ccre.log.LoggingTarget;
import ccre.supercanvas.DraggableBoxComponent;
import ccre.supercanvas.Rendering;

/**
 * A component allowing for logging arbitrary text to a Logging Target.
 *
 * @author skeggsc
 */
public class LoggingTargetControlComponent extends DraggableBoxComponent {

    private static final long serialVersionUID = 512336748876581233L;
    private final LoggingTarget out;
    private final String name;
    private LogLevel level = LogLevel.INFO;
    private final StringBuilder contents = new StringBuilder();

    /**
     * Create a new LoggingTargetControlComponent with a LoggingTarget to write
     * to.
     *
     * @param cx the X coordinate.
     * @param cy the Y coordinate.
     * @param name the name of the input.
     * @param out the LoggingTarget to write to.
     */
    public LoggingTargetControlComponent(int cx, int cy, String name, LoggingTarget out) {
        super(cx, cy);
        this.name = name;
        this.out = out;
    }

    private void setHalfWidth(int halfWidth) {
        int min = getPanel().getWidth() / 4;
        if (halfWidth < this.halfWidth) {
            if (this.halfWidth > min) {
                halfWidth = Math.max(halfWidth, min);
            } else {
                return;
            }
        }
        this.centerX += halfWidth - this.halfWidth;
        this.halfWidth = halfWidth;
    }

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        int conHeight = g.getFontMetrics().getHeight();
        g.setFont(Rendering.labels);
        String render = "[" + level + "] " + contents.toString();
        setHalfWidth(5 + Math.max(g.getFontMetrics().stringWidth(render) / 2, g.getFontMetrics(Rendering.console).stringWidth(name)));
        this.halfHeight = g.getFontMetrics().getHeight() / 2 + conHeight / 2;
        if (getPanel().editing == contents || getPanel().editmode) {
            Rendering.drawBody(getPanel().editing == contents ? Color.GREEN : Color.YELLOW, g, this);
        }
        g.setColor(Color.BLACK);
        g.drawString(render, centerX - halfWidth + 5, centerY + 5 + conHeight / 2);
        int yh = g.getFontMetrics().getHeight();
        g.setColor(Color.BLACK);
        g.setFont(Rendering.console);
        g.drawString(name, centerX - halfWidth + 5, centerY + 5 - yh / 2);
    }

    @Override
    public void onPressedEnter() {
        if (getPanel().editing == contents) {
            if (contents.length() == 0) {
                level = level.next();
                return;
            }
            out.log(level, contents.toString(), (String) null);
            contents.setLength(0);
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
}
