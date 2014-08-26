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

import ccre.channel.EventOutput;
import intelligence.monitor.IPhidgetMonitor;
import intelligence.monitor.VirtualPhidgetMonitor;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.io.Serializable;

/**
 * A component that represents a Phidget Monitor.
 *
 * @author skeggsc
 */
public class PhidgetMonitorComponent extends DraggableBoxComponent {

    private static final long serialVersionUID = -8989662604456982035L;
    private final IPhidgetMonitor monitor;
    private final String label;
    private boolean shared = false;

    /**
     * Create a new PhidgetMonitorComponent with a VirtualPhidgetMonitor.
     *
     * @param cx the X-coordinate.
     * @param cy the Y-coordinate.
     */
    public PhidgetMonitorComponent(int cx, int cy) {
        super(cx, cy);
        this.label = "VirtualPhidget";
        this.monitor = new VirtualPhidgetMonitor(new SerializableUnsharer());
    }

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        halfWidth = Math.max(70, fontMetrics.stringWidth(label) / 2);
        halfHeight = fontMetrics.getHeight() / 2 + 1;
        GradientPaint gp = new GradientPaint(centerX, centerY, Color.RED, centerX + halfHeight, centerY - halfHeight, Color.ORANGE);
        g.setPaint(gp);
        Shape s = new RoundRectangle2D.Float(centerX - halfWidth, centerY - halfHeight, halfWidth * 2, halfHeight * 2, 15, 15);
        g.fill(s);
        g.setColor(Color.BLACK);
        g.drawString(label, centerX - halfWidth + 5, centerY - halfHeight + 1 + fontMetrics.getAscent());
    }

    @Override
    public boolean onInteract(int x, int y) {
        return false;
    }

    @Override
    public String toString() {
        return "Phidget Monitor: " + label;
    }

    @Override
    protected void onChangePanel(SuperCanvasPanel panel) {
        boolean hasPanel = panel != null;
        if (hasPanel != shared) {
            if (hasPanel) {
                monitor.share();
            } else {
                monitor.unshare();
            }
            shared = hasPanel;
        }
    }

    private class SerializableUnsharer implements EventOutput, Serializable {

        private static final long serialVersionUID = 3550462781276699410L;

        @Override
        public void event() {
            getPanel().remove(PhidgetMonitorComponent.this);
        }
    }

}
