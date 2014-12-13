/*
 * Copyright 2014 Colby Skeggs
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
package ccre.igneous;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;

public abstract class DeviceComponent {

    private Device parent;
    protected Shape hitzone;

    void setDevice(Device parent) {
        this.parent = parent;
    }

    protected void repaint() {
        if (parent != null) {
            parent.repaint();
        }
    }

    public abstract int render(Graphics2D g, int width, int height, FontMetrics fontMetrics, int mouseX, int mouseY, int lastShift);

    public void checkPress(int x, int y) {
        if (hitzone != null && hitzone.contains(x, y)) {
            onPress(x, y);
        }
    }

    protected void onPress(int x, int y) {
        // Do nothing by default.
    }

    public void onMouseMove(int x, int y) {
        // Do nothing by default.
    }

    public void onMouseEnter(int x, int y) {
        // Do nothing by default.
    }

    public void onMouseExit(int x, int y) {
        // Do nothing by default.
    }

    public void onRelease(int x, int y) {
        // Do nothing by default.
    }
}
