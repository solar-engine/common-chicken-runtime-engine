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
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

/**
 * A base component of channel components, such as float, boolean, and event
 * inputs and outputs.
 *
 * @author skeggsc
 */
public abstract class BaseChannelComponent extends DraggableBoxComponent {

    /**
     * The name of this channel as it is displayed on the box.
     */
    protected final String name;

    /**
     * Create a new BaseChannelComponent.
     *
     * @param cx the X coordinate.
     * @param cy the Y coordinate.
     * @param name the name of the input.
     */
    public BaseChannelComponent(int cx, int cy, String name) {
        super(cx, cy);
        this.name = name;
    }

    @Override
    public final void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        halfWidth = Math.max(70, g.getFontMetrics().stringWidth(name) / 2);
        halfHeight = halfWidth * 2 / 3;
        g.setPaint(new GradientPaint(centerX, centerY, Color.YELLOW, centerX + halfHeight, centerY - halfHeight, Color.ORANGE));
        g.fill(new RoundRectangle2D.Float(centerX - halfWidth, centerY - halfHeight, halfWidth * 2, halfHeight * 2, 15, 15));
        g.setColor(Color.BLACK);
        g.drawString(name, centerX - halfWidth + 5, centerY - halfHeight + 1 + g.getFontMetrics().getAscent());
        channelRender(g, screenWidth, screenHeight, fontMetrics, mouseX, mouseY);
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Called to render the channel-specific part of this component.
     *
     * @param g The graphics pen to use to render.
     * @param screenWidth The width of the screen.
     * @param screenHeight The height of the screen.
     * @param fontMetrics The metrics of the (monospaced) font.
     * @param mouseX The mouse X position.
     * @param mouseY The mouse Y position.
     */
    protected abstract void channelRender(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY);
}
