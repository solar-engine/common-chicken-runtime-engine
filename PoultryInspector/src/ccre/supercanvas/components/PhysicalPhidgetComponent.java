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
import ccre.supercanvas.SuperCanvasPanel;
import ccre.supercanvas.phidget.PhidgetMonitor;

/**
 * A component that represents a Phidget Monitor.
 *
 * @author skeggsc
 */
public class PhysicalPhidgetComponent extends DraggableBoxComponent {

    private static final long serialVersionUID = -8989662604456982035L;
    private final PhidgetMonitor monitor = new PhidgetMonitor();
    private boolean shared = false;

    /**
     * Create a new PhidgetMonitorComponent.
     *
     * @param cx the X-coordinate.
     * @param cy the Y-coordinate.
     */
    public PhysicalPhidgetComponent(int cx, int cy) {
        super(cx, cy, true);
    }

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        halfWidth = 70;
        halfHeight = fontMetrics.getHeight() / 2 + 1;
        Rendering.drawBody(Color.RED, g, this);
        g.setColor(Color.BLACK);
        g.drawString("PhysicalPhidget", centerX - halfWidth + 5, centerY - halfHeight + 1 + fontMetrics.getAscent());
    }

    @Override
    public boolean onInteract(int x, int y) {
        return false;
    }

    @Override
    public String toString() {
        return "Phidget Monitor";
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
