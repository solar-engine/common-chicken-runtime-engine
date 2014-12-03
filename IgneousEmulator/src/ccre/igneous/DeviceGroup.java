package ccre.igneous;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import ccre.concurrency.ConcurrentDispatchArray;

public class DeviceGroup extends Device {
    private final ConcurrentDispatchArray<Device> devices = new ConcurrentDispatchArray<Device>();

    public synchronized void add(Device device) {
        devices.add(device);
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
