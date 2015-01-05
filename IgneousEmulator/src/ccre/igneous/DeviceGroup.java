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

import ccre.concurrency.ConcurrentDispatchArray;

/**
 * A set of related devices grouped into one larger device for display purposes.
 * 
 * @author skeggsc
 */
public class DeviceGroup extends Device {
    private final ConcurrentDispatchArray<Device> devices = new ConcurrentDispatchArray<Device>();

    /**
     * Add a device to this group.
     * 
     * @param device the device to add.
     * @return the device that was just added, for method chaining.
     */
    public synchronized <D extends Device> D add(D device) {
        devices.add(device);
        return device;
    }

    @Override
    public void render(Graphics2D g, int width, int height, FontMetrics fontMetrics, int mouseX, int mouseY) {
        super.render(g, width, height, fontMetrics, mouseX, mouseY);
        int y = 10;
        g.translate(10, 0);
        for (Device d : devices) {
            int deviceHeight = d.getHeight();
            g.translate(0, y);
            d.render(g, width - 10, deviceHeight, fontMetrics, mouseX - 10, mouseY - y);
            g.translate(0, -y);
            y += deviceHeight;
        }
        g.translate(-10, 0);
    }

    protected Color getBackgroundColor() {
        return Color.LIGHT_GRAY;
    }

    public int getHeight() {
        int total = 20;
        for (Device d : devices) {
            total += d.getHeight();
        }
        return total;
    }

    public void onPress(int x, int y) {
        if (x >= 10) {
            y -= 10;
            for (Device dev : devices) {
                int height = dev.getHeight();
                if (y >= 0 && y < height) {
                    dev.onPress(x - 10, y);
                }
                y -= height;
            }
        }
    }

    public void onRelease(int x, int y) {
        if (x >= 10) {
            y -= 10;
            for (Device dev : devices) {
                int height = dev.getHeight();
                if (y >= 0 && y < height) {
                    dev.onRelease(x - 10, y);
                }
                y -= height;
            }
        }
    }

    public void onMouseMove(int x, int y) {
        if (x >= 10) {
            y -= 10;
            for (Device dev : devices) {
                int height = dev.getHeight();
                if (y >= 0 && y < height) {
                    dev.onMouseMove(x - 10, y);
                }
                y -= height;
            }
        }
    }

    public void onMouseEnter(int x, int y) {
        if (x >= 10) {
            y -= 10;
            for (Device dev : devices) {
                int height = dev.getHeight();
                if (y >= 0 && y < height) {
                    dev.onMouseEnter(x - 10, y);
                }
                y -= height;
            }
        }
    }

    public void onMouseExit(int x, int y) {
        if (x >= 10) {
            y -= 10;
            for (Device dev : devices) {
                int height = dev.getHeight();
                if (y >= 0 && y < height) {
                    dev.onMouseExit(x - 10, y);
                }
                y -= height;
            }
        }
    }
}
