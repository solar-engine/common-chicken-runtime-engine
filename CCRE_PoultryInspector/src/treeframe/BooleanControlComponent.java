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

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;

public class BooleanControlComponent extends DraggableBoxComponent {

    private boolean pressed;
    private final String name;

    public BooleanControlComponent(int cx, int cy, String name) {
        super(cx, cy);
        this.name = name;
    }

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        width = Math.max(70, g.getFontMetrics().stringWidth(name) / 2);
        height = width * 2 / 3;
        GradientPaint gp = new GradientPaint(centerX, centerY, Color.YELLOW, centerX + height, centerY - height, Color.ORANGE);
        ((Graphics2D) g).setPaint(gp);
        Shape s = new RoundRectangle2D.Float(centerX - width, centerY - height, width * 2, height * 2, 15, 15);
        ((Graphics2D) g).fill(s);
        g.setColor(Color.BLACK);
        g.drawString(name, centerX - width + 5, centerY - height + 1 + g.getFontMetrics().getAscent());
        AffineTransform origO = g.getTransform();
        {
            g.setColor(pressed ? Color.GREEN.darker() : Color.RED.darker());
            AffineTransform orig = g.getTransform();
            g.rotate(pressed ? 10 : -10, centerX + (pressed ? 3 : -3), centerY + 10);
            g.fillRect(centerX - 5, centerY + 5, 10, 45);
            g.setTransform(orig);
            g.setColor(Color.GRAY.darker().darker());
            g.fillRect(centerX - 20, centerY + 10, 40, 20);
        }
        g.translate(-5, 2);
        {
            g.setColor(pressed ? Color.GREEN : Color.RED);
            AffineTransform orig = g.getTransform();
            g.rotate(pressed ? 10 : -10, centerX + (pressed ? 3 : -3), centerY + 10);
            g.fillRect(centerX - 5, centerY + 5, 10, 45);
            g.setTransform(orig);
            g.setColor(Color.GRAY.darker());
            g.fillRect(centerX - 20, centerY + 10, 40, 20);
        }
        g.setTransform(origO);
    }

    @Override
    public boolean onInteract(int x, int y) {
        pressed = !pressed;
        return true;
    }

    public String toString() {
        return name;
    }
}
