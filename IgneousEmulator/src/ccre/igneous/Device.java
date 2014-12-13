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

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;

public class Device {
    
    private DeviceListPanel parent;
    private final ArrayList<DeviceComponent> components = new ArrayList<DeviceComponent>();
    
    void setParent(DeviceListPanel parent) {
        this.parent = parent;
    }
    
    protected void add(DeviceComponent component) {
        component.setDevice(this);
        components.add(component);
        this.repaint();
    }
    
    protected void repaint() {
        if (parent != null) {
            parent.repaint();
        }
    }

    public void render(Graphics2D g, int width, int height, FontMetrics fontMetrics, int mouseX, int mouseY) {
        g.setColor(getBackgroundColor());
        g.fillRect(0, 0, width, height);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, width - 1, height - 1);
        int lastShift = 0;
        for (DeviceComponent component : components) {
            lastShift = component.render(g, width, height, fontMetrics, mouseX, mouseY, lastShift);
        }
    }

    protected Color getBackgroundColor() {
        return Color.GRAY;
    }

    public int getHeight() {
        return 50;
    }

    public void onPress(int x, int y) {
        for (DeviceComponent component : components) {
            component.checkPress(x, y);
        }
    }

    public void onRelease(int x, int y) {
        for (DeviceComponent component : components) {
            component.onRelease(x, y);
        }
    }

    public void onMouseMove(int x, int y) {
        for (DeviceComponent component : components) {
            component.onMouseMove(x, y);
        }
    }

    public void onMouseEnter(int x, int y) {
        for (DeviceComponent component : components) {
            component.onMouseEnter(x, y);
        }
    }

    public void onMouseExit(int x, int y) {
        for (DeviceComponent component : components) {
            component.onMouseExit(x, y);
        }
    }
}
