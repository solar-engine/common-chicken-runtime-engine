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
package ccre.supercanvas.components.channels;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.channel.FloatStatus;
import ccre.ctrl.FloatMixing;
import ccre.supercanvas.BaseChannelComponent;

/**
 * A component allowing interaction with floats.
 *
 * @author skeggsc
 */
public class FloatControlComponent extends BaseChannelComponent implements FloatInput {

    private static final long serialVersionUID = 8379882900431074283L;
    private final FloatStatus stat = new FloatStatus();
    private final FloatOutput rawOut;
    private boolean hasSentInitial = false;

    /**
     * Create a new FloatControlComponent with a FloatOutput to control.
     *
     * @param cx the X coordinate.
     * @param cy the Y coordinate.
     * @param name the name of the output.
     * @param out the FloatOutput to control.
     */
    public FloatControlComponent(int cx, int cy, String name, FloatOutput out) {
        super(cx, cy, name);
        rawOut = out;
    }

    /**
     * Create a new FloatControlComponent.
     *
     * @param cx the X coordinate.
     * @param cy the Y coordinate.
     * @param name the name of the output.
     */
    public FloatControlComponent(int cx, int cy, String name) {
        super(cx, cy, name);
        rawOut = FloatMixing.ignoredFloatOutput;
    }

    @Override
    protected boolean containsForInteract(int x, int y) {
        return x >= centerX - halfWidth + 10 && x <= centerX + halfWidth - 10 && y >= centerY - halfHeight / 2 && y <= centerY + halfHeight / 2;
    }

    @Override
    public void channelRender(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        g.setColor(Color.WHITE);
        g.fillRect(centerX - halfWidth + 10, centerY - halfHeight / 2, 2 * halfWidth - 19, halfHeight);
        g.setColor(Color.BLACK);
        g.drawRect(centerX - halfWidth + 10, centerY - halfHeight / 2, 2 * halfWidth - 20, halfHeight - 1);
        g.drawLine(centerX, centerY + halfHeight / 2 - 1, centerX, centerY + 5);
        g.drawLine(centerX + halfWidth * 2 / 3, centerY + halfHeight / 2 - 1, centerX + halfWidth * 2 / 3, centerY + 5);
        g.drawLine(centerX - halfWidth * 2 / 3, centerY + halfHeight / 2 - 1, centerX - halfWidth * 2 / 3, centerY + 5);
        g.drawLine(centerX - halfWidth / 6, centerY + halfHeight / 2 - 1, centerX - halfWidth / 6, centerY + 15);
        g.drawLine(centerX + halfWidth / 6, centerY + halfHeight / 2 - 1, centerX + halfWidth / 6, centerY + 15);
        g.drawLine(centerX - halfWidth / 3, centerY + halfHeight / 2 - 1, centerX - halfWidth / 3, centerY + 10);
        g.drawLine(centerX + halfWidth / 3, centerY + halfHeight / 2 - 1, centerX + halfWidth / 3, centerY + 10);
        g.drawLine(centerX - 3 * halfWidth / 6, centerY + halfHeight / 2 - 1, centerX - 3 * halfWidth / 6, centerY + 15);
        g.drawLine(centerX + 3 * halfWidth / 6, centerY + halfHeight / 2 - 1, centerX + 3 * halfWidth / 6, centerY + 15);
        if (hasSentInitial) {
            float value = this.stat.get();
            int ptrCtr = centerX + (int) (halfWidth * 2 / 3 * value);
            if (value < 0) {
                g.setColor(value == -1 ? Color.RED : Color.RED.darker().darker());
            } else if (value > 0) {
                g.setColor(value == 1 ? Color.GREEN : Color.GREEN.darker().darker());
            } else {
                g.setColor(Color.ORANGE);
            }
            g.drawPolygon(new int[] { ptrCtr - 12, ptrCtr - 8, ptrCtr - 12 }, new int[] { centerY - 8, centerY - 4, centerY }, 3);
            g.drawPolygon(new int[] { ptrCtr + 12, ptrCtr + 8, ptrCtr + 12 }, new int[] { centerY - 8, centerY - 4, centerY }, 3);
            g.fillRect(ptrCtr - 5, centerY - halfHeight / 2 + 1, 11, halfHeight / 2 - 4);
            g.fillPolygon(new int[] { ptrCtr - 5, ptrCtr, ptrCtr + 6 }, new int[] { centerY - 3, centerY + 3, centerY - 3 }, 3);
        }
    }

    @Override
    public boolean wantsDragSelect() {
        return true;
    }

    @Override
    public boolean onInteract(int x, int y) {
        if (isBeingDragged()) {
            return false;
        }
        float value = Math.min(1, Math.max(-1, (x - centerX) / (float) (halfWidth * 2 / 3)));
        if (-0.1 < value && value < 0.1) {
            value = 0;
        }
        if (value != stat.get() || !hasSentInitial) {
            stat.set(value);
            if (!hasSentInitial) {
                stat.send(rawOut);
                hasSentInitial = true;
            }
        }
        return true;
    }

    @Override
    public void send(FloatOutput output) {
        stat.send(output);
    }

    @Override
    public void unsend(FloatOutput output) {
        stat.unsend(output);
    }

    @Override
    public float get() {
        return stat.get();
    }

    @Override
    public boolean canDragInteract() {
        return true;
    }
}
