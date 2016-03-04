/*
 * Copyright 2014-2015 Cel Skeggs
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
package ccre.frc;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;

/**
 * A component in a Device. These exist so that devices can be rebuilt from the
 * various common components that make them up.
 *
 * @author skeggsc
 */
public abstract class DeviceComponent {

    private Device parent;
    /**
     * The zone checked by the default {@link #checkPress(int, int)}
     * implementation to see if {@link #onPress(int, int)} should be called.
     * Specifically, <code>onPress</code> is only called if the mouse was in
     * this zone.
     *
     * If null (the default), this means that no mouse presses will translate
     * into calls to <code>onPress</code>.
     */
    protected Shape hitzone;

    void setDevice(Device parent) {
        this.parent = parent;
    }

    /**
     * Notify this DeviceComponent, and thereby the containing window, that
     * something has changed, and the screen should refresh.
     */
    protected void repaint() {
        if (parent != null) {
            parent.repaint();
        }
    }

    /**
     * Render the component in the device, given a specified shift for layout.
     *
     * @param g the graphics to render on.
     * @param width the width of the device.
     * @param height the height of the device.
     * @param fontMetrics the FontMetrics of the original font.
     * @param mouseX the mouse position X relative to the device.
     * @param mouseY the mouse position Y relative to the device.
     * @param lastShift the shift for layout - usually where to start drawing in
     * the X-axis.
     * @return the new shift for layout - usually where the next component
     * should start drawing in the X-axis.
     */
    public abstract int render(Graphics2D g, int width, int height, FontMetrics fontMetrics, int mouseX, int mouseY, int lastShift);

    /**
     * Checks if the given position is over this component and handles a press
     * event if it is.
     *
     * @param x the X position relative to the device.
     * @param y the Y position relative to the device.
     */
    public void checkPress(int x, int y) {
        if (hitzone != null && hitzone.contains(x, y)) {
            onPress(x, y);
        }
    }

    /**
     * Called by the default implementation of {@link #checkPress(int, int)} if
     * the mouse is within the {@link #hitzone}.
     *
     * @param x the x coordinate of the mouse, relative to the device.
     * @param y the y coordinate of the mouse, relative to the device.
     */
    protected void onPress(int x, int y) {
        // Do nothing by default.
    }

    /**
     * Called when the mouse moves within the device.
     *
     * @param x the new mouse X.
     * @param y the new mouse Y.
     */
    public void onMouseMove(int x, int y) {
        // Do nothing by default.
    }

    /**
     * Called when the mouse enters the device.
     *
     * @param x the new mouse X.
     * @param y the new mouse Y.
     */
    public void onMouseEnter(int x, int y) {
        // Do nothing by default.
    }

    /**
     * Called when the mouse exits the device.
     *
     * @param x the new mouse X.
     * @param y the new mouse Y.
     */
    public void onMouseExit(int x, int y) {
        // Do nothing by default.
    }

    /**
     * Called when the mouse is released within the device.
     *
     * @param x the new mouse X.
     * @param y the new mouse Y.
     */
    public void onRelease(int x, int y) {
        // Do nothing by default.
    }
}
