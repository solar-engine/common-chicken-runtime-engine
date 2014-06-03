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
package treeframe;

import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

public class EventControlComponent extends DraggableBoxComponent implements EventInput {

    private long countStart;
    private final String name;
    private final EventStatus stat = new EventStatus();

    public EventControlComponent(int cx, int cy, String name) {
        super(cx, cy);
        this.name = name;
    }

    public EventControlComponent(int cx, int cy, String name, EventOutput event) {
        this(cx, cy, name);
        stat.send(event);
    }

    @Override
    protected boolean containsForInteract(int x, int y) {
        return x >= centerX - halfWidth / 3 - 10 && x <= centerX + halfWidth / 3 + 10 && y >= centerY - halfHeight / 3 - 10 && y <= centerY + halfHeight / 3 + 20;
    }

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        halfWidth = Math.max(70, g.getFontMetrics().stringWidth(name) / 2);
        halfHeight = halfWidth * 2 / 3;
        GradientPaint gp = new GradientPaint(centerX, centerY, Color.YELLOW, centerX + halfHeight, centerY - halfHeight, Color.ORANGE);
        ((Graphics2D) g).setPaint(gp);
        Shape s = new RoundRectangle2D.Float(centerX - halfWidth, centerY - halfHeight, halfWidth * 2, halfHeight * 2, 15, 15);
        ((Graphics2D) g).fill(s);
        g.setColor(Color.BLACK);
        g.drawString(name, centerX - halfWidth + 5, centerY - halfHeight + 1 + g.getFontMetrics().getAscent());
        long count = (System.currentTimeMillis() - countStart);
        g.setColor(Color.ORANGE.darker());
        int rel = count < 200 ? 3 : 10;
        g.fillOval(centerX - halfWidth / 3, 10 + centerY - halfHeight / 3, 2 * halfWidth / 3, 2 * halfHeight / 3);
        g.fillRect(centerX - halfWidth / 3 + 1, 10 + centerY - rel, 2 * halfWidth / 3 - 1, rel);
        g.setColor(count < 200 ? Color.GREEN : Color.RED);
        g.fillOval(centerX - halfWidth / 3, 10 + centerY - halfHeight / 3 - rel, 2 * halfWidth / 3, 2 * halfHeight / 3);
    }

    @Override
    public boolean onInteract(int x, int y) {
        stat.event();
        countStart = System.currentTimeMillis();
        return true;
    }

    public String toString() {
        return name;
    }

    @Override
    public void send(EventOutput listener) {
        stat.send(listener);
    }

    @Override
    public void unsend(EventOutput listener) {
        stat.unsend(listener);
    }
}
