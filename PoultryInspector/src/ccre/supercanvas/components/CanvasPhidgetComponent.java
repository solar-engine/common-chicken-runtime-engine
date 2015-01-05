/*
 * Copyright 2014-2015 Colby Skeggs.
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

import static ccre.supercanvas.phidget.PhidgetMonitor.ANALOG_COUNT;
import static ccre.supercanvas.phidget.PhidgetMonitor.INPUT_COUNT;
import static ccre.supercanvas.phidget.PhidgetMonitor.LCD_LINES;
import static ccre.supercanvas.phidget.PhidgetMonitor.OUTPUT_COUNT;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.BooleanStatus;
import ccre.channel.FloatInput;
import ccre.channel.FloatStatus;
import ccre.cluck.Cluck;
import ccre.holders.StringHolder;
import ccre.supercanvas.DraggableBoxComponent;
import ccre.supercanvas.Rendering;
import ccre.supercanvas.SuperCanvasPanel;
import ccre.util.LineCollectorOutputStream;

/**
 * A SuperCanvas-based component to display a virtual Phidget.
 * 
 * @author skeggsc
 */
public class CanvasPhidgetComponent extends DraggableBoxComponent {

    private static final long serialVersionUID = -4281545286901323041L;
    private final BooleanStatus attached = new BooleanStatus();
    private final StringHolder[] lcdLines = new StringHolder[2];
    private final boolean[] displayLights = new boolean[8];
    private final BooleanStatus[] inputButtons = new BooleanStatus[8];
    private final FloatStatus[] sliders = new FloatStatus[8];
    private final int[] componentXs = new int[16], componentYs = new int[16];
    private int boxWidth, boxHeight, barWidth, barHeight;

    /**
     * Create a new CanvasPhidgetComponent at the given location.
     * 
     * @param cx the X position.
     * @param cy the Y position.
     */
    public CanvasPhidgetComponent(int cx, int cy) {
        super(cx, cy);
        for (int i = 0; i < lcdLines.length; i++) {
            lcdLines[i] = new StringHolder("....................");
        }
        for (int i = 0; i < inputButtons.length; i++) {
            inputButtons[i] = new BooleanStatus();
        }
        for (int i = 0; i < sliders.length; i++) {
            sliders[i] = new FloatStatus();
        }
        halfWidth = 80;
        halfHeight = 175;
    }

    private void share() {
        attached.set(true);
        for (int i = 0; i < OUTPUT_COUNT; i++) {
            final int localI = i;
            Cluck.publish("phidget-bo" + i, new BooleanOutput() {
                public void set(boolean value) {
                    displayLights[localI] = value;
                }
            });
        }
        for (int i = 0; i < LCD_LINES; i++) {
            final int localI = i;
            @SuppressWarnings("resource")
            LineCollectorOutputStream collector = new LineCollectorOutputStream() {
                @Override
                protected void collect(final String str) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            lcdLines[localI].set((str + "                    ").substring(0, 20));
                        }
                    });
                }
            };
            Cluck.publish("phidget-lcd" + i, collector);
        }
        Cluck.publish("phidget-attached", (BooleanInput) attached);
        for (int i = 0; i < INPUT_COUNT; i++) {
            Cluck.publish("phidget-bi" + i, (BooleanInput) inputButtons[i]);
        }
        for (int i = 0; i < ANALOG_COUNT; i++) {
            Cluck.publish("phidget-ai" + i, (FloatInput) sliders[i]);
        }
        Cluck.getNode().notifyNetworkModified();
    }

    private void unshare() {
        attached.set(false);
        Cluck.getNode().removeLink("phidget-attached");
        for (int i = 0; i < OUTPUT_COUNT; i++) {
            Cluck.getNode().removeLink("phidget-bo" + i);
        }
        for (int i = 0; i < LCD_LINES; i++) {
            Cluck.getNode().removeLink("phidget-lcd" + i);
        }
        for (int i = 0; i < INPUT_COUNT; i++) {
            Cluck.getNode().removeLink("phidget-bi" + i);
        }
        for (int i = 0; i < ANALOG_COUNT; i++) {
            Cluck.getNode().removeLink("phidget-ai" + i);
        }
        Cluck.getNode().notifyNetworkModified();
    }

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        Rendering.drawBody(Color.RED, g, this);
        g.setColor(Color.BLACK);
        int baseY = centerY - halfHeight + 1 + fontMetrics.getAscent();
        g.drawString("VirtualPhidget", centerX - g.getFontMetrics().stringWidth("VirtualPhidget") / 2, baseY);
        for (StringHolder line : lcdLines) {
            baseY += g.getFontMetrics().getHeight();
            g.drawString(line.get(), centerX - g.getFontMetrics().stringWidth(line.get()) / 2, baseY);
        }
        baseY += 10;
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 4; x++) {
                g.setColor(!displayLights[x + y * 4] ? Color.YELLOW : Color.BLACK); // Invert because that's what happens with the real Phidget.
                g.fillOval(centerX - halfWidth + ((x * 4 + 4) * halfWidth / 10) - halfWidth / 8, baseY, halfWidth / 4, halfWidth / 4);
            }
            baseY += (3 * halfWidth / 10);
        }
        boxWidth = boxHeight = halfWidth / 4;
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 4; x++) {
                g.setColor(inputButtons[x + y * 4].get() ? Color.WHITE : Color.BLACK);
                int cornerX = centerX - halfWidth + ((x + 1) * 2 * halfWidth / 5) - boxWidth / 2;
                g.fillRect(cornerX, baseY, boxWidth, boxHeight);
                componentXs[x + y * 4] = cornerX;
                componentYs[x + y * 4] = baseY;
            }
            baseY += (3 * halfWidth / 10);
        }
        baseY += 10;
        barWidth = halfWidth * 2 - 10;
        barHeight = halfWidth / 5;
        for (int y = 0; y < 8; y++) {
            g.setColor(Color.BLACK);
            int cornerX = centerX - halfWidth + 5;
            g.fillRect(cornerX, baseY, barWidth, barHeight);
            g.setColor(Color.BLUE);
            float value = sliders[y].get();
            if (value > 0) {
                g.fillRect(cornerX + barWidth / 2, baseY, Math.round(value * barWidth / 2), barHeight);
            } else {
                g.fillRect(Math.round(cornerX + barWidth / 2 + value * barWidth / 2), baseY, Math.round(-value * barWidth / 2), barHeight);
            }
            componentXs[8 + y] = cornerX;
            componentYs[8 + y] = baseY;
            baseY += (3 * halfWidth / 10);
        }
    }

    @Override
    public boolean onInteract(int x, int y) {
        for (int i = 0; i < 8; i++) {
            if (x >= componentXs[i] && x < componentXs[i] + boxWidth && y >= componentYs[i] && y < componentYs[i] + boxHeight) {
                inputButtons[i].set(!inputButtons[i].get());
                getPanel().repaint();
                return true;
            }
        }
        for (int i = 8; i < 16; i++) {
            if (x >= componentXs[i] - 2 && x < componentXs[i] + barWidth + 2 && y >= componentYs[i] && y < componentYs[i] + barHeight) {
                float xScale = (x - componentXs[i]) / (float) barWidth;
                sliders[i - 8].set(Math.min(Math.max(xScale * 2 - 1, -1), 1));
                getPanel().repaint();
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean canDragInteract() {
        return true;
    }

    @Override
    protected boolean containsForInteract(int x, int y) {
        for (int i = 0; i < 8; i++) {
            if (x >= componentXs[i] && x < componentXs[i] + boxWidth && y >= componentYs[i] && y < componentYs[i] + boxHeight) {
                return true;
            }
        }
        for (int i = 8; i < 16; i++) {
            if (x >= componentXs[i] - 2 && x < componentXs[i] + barWidth + 2 && y >= componentYs[i] && y < componentYs[i] + barHeight) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Canvas Phidget Monitor";
    }

    @Override
    protected void onChangePanel(SuperCanvasPanel panel) {
        boolean hasPanel = panel != null;
        if (hasPanel != attached.get()) {
            if (hasPanel) {
                share();
            } else {
                unshare();
            }
        }
    }
}
