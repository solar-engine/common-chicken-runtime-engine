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

import intelligence.monitor.IPhidgetMonitor;
import intelligence.monitor.PhidgetMonitor;
import intelligence.monitor.VirtualPhidgetMonitor;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import ccre.channel.EventOutput;

/**
 * A component that represents a Phidget Monitor.
 *
 * @author skeggsc
 * @param <M> The subtype of IPhidgetMonitor that this contains
 */
public class PhidgetMonitorComponent<M extends IPhidgetMonitor> extends DraggableBoxComponent {
    
    public static class VirtualPhidget extends PhidgetMonitorComponent<VirtualPhidgetMonitor> implements EventOutput {
        private static final long serialVersionUID = -3640780784824672778L;

        public VirtualPhidget(int cx, int cy) {
            super(cx, cy, "VirtualPhidget", new VirtualPhidgetMonitor());
            monitor.setCloseEvent(this);
        }
        @Override
        public void event() {
            getPanel().remove(this);
        }
    }

    public static class PhysicalPhidget extends PhidgetMonitorComponent<PhidgetMonitor> {
        private static final long serialVersionUID = 1440999334054971431L;

        public PhysicalPhidget(int cx, int cy) {
            super(cx, cy, "PhysicalPhidget", new PhidgetMonitor());
        }
    }

    private static final long serialVersionUID = -8989662604456982035L;
    protected final M monitor;
    private final String label;
    private boolean shared = false;

    /**
     * Create a new PhidgetMonitorComponent with a VirtualPhidgetMonitor.
     *
     * @param cx the X-coordinate.
     * @param cy the Y-coordinate.
     * @param monitor the monitor to display.
     */
    private PhidgetMonitorComponent(int cx, int cy, String label, M monitor) {
        super(cx, cy, true);
        this.label = label;
        this.monitor = monitor;
    }

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        halfWidth = Math.max(70, fontMetrics.stringWidth(label) / 2);
        halfHeight = fontMetrics.getHeight() / 2 + 1;
        Rendering.drawBody(Color.RED, g, this);
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
}
