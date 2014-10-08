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

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;

/**
 * A component allowing display of floats.
 *
 * @author skeggsc
 */
public class FloatDisplayComponent extends BaseChannelComponent implements FloatOutput {

    private static final long serialVersionUID = 4027452153991095626L;
    private float value;
    private boolean subscribed;
    private final FloatInput inp;

    /**
     * Create a new FloatDisplayComponent with a FloatInput to read from.
     *
     * @param cx the X coordinate.
     * @param cy the Y coordinate.
     * @param name the name of the input.
     * @param inp the FloatInput to read from.
     */
    public FloatDisplayComponent(int cx, int cy, String name, FloatInput inp) {
        super(cx, cy, name);
        this.inp = inp;
    }

    /**
     * Create a new FloatDisplayComponent.
     *
     * @param cx the X coordinate.
     * @param cy the Y coordinate.
     * @param name the name of the input.
     */
    public FloatDisplayComponent(int cx, int cy, String name) {
        this(cx, cy, name, null);
    }

    @Override
    public void channelRender(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        g.setColor(Color.WHITE);
        g.fillRect(centerX - halfWidth + 10, centerY - halfHeight / 2, 2 * halfWidth - 20, halfHeight);
        g.setColor(Color.BLACK);
        g.drawRect(centerX - halfWidth + 10, centerY - halfHeight / 2, 2 * halfWidth - 21, halfHeight - 1);
        g.drawLine(centerX, centerY + halfHeight / 2 - 1, centerX, centerY + 5);
        g.drawLine(centerX + halfWidth * 2 / 3, centerY + halfHeight / 2 - 1, centerX + halfWidth * 2 / 3, centerY + 5);
        g.drawLine(centerX - halfWidth * 2 / 3, centerY + halfHeight / 2 - 1, centerX - halfWidth * 2 / 3, centerY + 5);
        g.drawLine(centerX - halfWidth / 6, centerY + halfHeight / 2 - 1, centerX - halfWidth / 6, centerY + 15);
        g.drawLine(centerX + halfWidth / 6, centerY + halfHeight / 2 - 1, centerX + halfWidth / 6, centerY + 15);
        g.drawLine(centerX - halfWidth / 3, centerY + halfHeight / 2 - 1, centerX - halfWidth / 3, centerY + 10);
        g.drawLine(centerX + halfWidth / 3, centerY + halfHeight / 2 - 1, centerX + halfWidth / 3, centerY + 10);
        g.drawLine(centerX - 3 * halfWidth / 6, centerY + halfHeight / 2 - 1, centerX - 3 * halfWidth / 6, centerY + 15);
        g.drawLine(centerX + 3 * halfWidth / 6, centerY + halfHeight / 2 - 1, centerX + 3 * halfWidth / 6, centerY + 15);
        if (value != 0) {
            String strv = Float.toString(value);
            g.drawString(strv, value > 0 ? centerX - fontMetrics.stringWidth(strv) - 10 : centerX + 10, centerY - halfHeight / 2 + fontMetrics.getHeight());
        }
        int ptrCtr = centerX + (int) (halfWidth * 2 / 3 * value);
        if (Math.abs(value) <= 1.1) {
            if (value < 0) {
                g.setColor(value == -1 ? Color.RED : Color.RED.darker().darker());
            } else if (value > 0) {
                g.setColor(value == 1 ? Color.GREEN : Color.GREEN.darker().darker());
            } else {
                g.setColor(Color.ORANGE);
            }
        }
        g.fillRect(ptrCtr - 5, centerY - halfHeight / 2 + 1, 11, halfHeight / 2 - 4);
        g.fillPolygon(new int[] { ptrCtr - 5, ptrCtr, ptrCtr + 6 }, new int[] { centerY - 3, centerY + 3, centerY - 3 }, 3);
    }

    @Override
    public boolean onInteract(int x, int y) {
        return false;
    }

    @Override
    protected void onChangePanel(SuperCanvasPanel panel) {
        boolean hasPanel = panel != null;
        if (inp != null && hasPanel != subscribed) {
            if (hasPanel) {
                inp.send(this);
            } else {
                inp.unsend(this);
            }
            subscribed = hasPanel;
        }
    }

    @Override
    public void set(float value) {
        this.value = value;
    }
}
