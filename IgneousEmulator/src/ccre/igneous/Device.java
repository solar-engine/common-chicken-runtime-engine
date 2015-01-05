/*
 * Copyright 2014-2015 Colby Skeggs
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

/**
 * An emulator device - something displayed on the emulator screen to view or
 * control part of the emulated robot's environment.
 * 
 * @author skeggsc
 */
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

    /**
     * Render the device.
     * 
     * @param g the graphics object to draw with.
     * @param width the width to draw in.
     * @param height the height to draw in.
     * @param fontMetrics the FontMetrics of the originally-set font.
     * @param mouseX the mouse position X, relative to the component.
     * @param mouseY the mouse position Y, relative to the component.
     */
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

    /**
     * Provide the height of space to reserve for the component. This may change
     * occasionally, but should generally remain constant between two adjacent
     * calls.
     * 
     * @return the height, in pixels.
     */
    public int getHeight() {
        return 50;
    }

    /**
     * Called when the device is pressed by the mouse.
     * 
     * @param x the relative mouse position X.
     * @param y the relative mouse position Y.
     */
    public void onPress(int x, int y) {
        for (DeviceComponent component : components) {
            component.checkPress(x, y);
        }
    }

    /**
     * Called when the device is released by the mouse.
     * 
     * @param x the relative mouse position X.
     * @param y the relative mouse position Y.
     */
    public void onRelease(int x, int y) {
        for (DeviceComponent component : components) {
            component.onRelease(x, y);
        }
    }

    /**
     * Called when the mouse moves above the device.
     * 
     * @param x the relative mouse position X.
     * @param y the relative mouse position Y.
     */
    public void onMouseMove(int x, int y) {
        for (DeviceComponent component : components) {
            component.onMouseMove(x, y);
        }
    }

    /**
     * Called when the mouse enters the device's area.
     * 
     * @param x the relative mouse position X.
     * @param y the relative mouse position Y.
     */
    public void onMouseEnter(int x, int y) {
        for (DeviceComponent component : components) {
            component.onMouseEnter(x, y);
        }
    }

    /**
     * Called when the mouse exits the device's area.
     * 
     * @param x the relative mouse position X.
     * @param y the relative mouse position Y.
     */
    public void onMouseExit(int x, int y) {
        for (DeviceComponent component : components) {
            component.onMouseExit(x, y);
        }
    }
}
