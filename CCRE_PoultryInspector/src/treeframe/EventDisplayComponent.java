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
import intelligence.Rendering;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

public class EventDisplayComponent extends DraggableBoxComponent implements EventOutput {

    private long countStart;
    private boolean subscribed;
    private final String name;
    private final EventInput inp;

    public EventDisplayComponent(int cx, int cy, String name) {
        this(cx, cy, name, null);
    }

    public EventDisplayComponent(int cx, int cy, String name, EventInput inp) {
        super(cx, cy);
        this.name = name;
        this.inp = inp;
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
        g.setColor(Rendering.blend(Color.green, Color.orange, count / 500.0f));
        int rad = Math.min(halfWidth / 3, halfHeight / 3);
        g.fillOval(centerX - rad, centerY - rad, rad * 2, rad * 2);
    }

    @Override
    public boolean onInteract(int x, int y) {
        return false;
    }

    public String toString() {
        return name;
    }

    @Override
    protected void onChangePanel(SuperCanvasPanel panel) {
        boolean hasPanel = panel != null;
        if (inp != null && hasPanel != subscribed) {
            if (hasPanel) {
                inp.send(this);
            } else {
                inp.unsend(this);
            }
            subscribed = hasPanel;
        }
    }

    @Override
    public void event() {
        countStart = System.currentTimeMillis();
    }
}
