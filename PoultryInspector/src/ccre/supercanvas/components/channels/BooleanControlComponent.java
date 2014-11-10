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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.BooleanStatus;
import ccre.ctrl.BooleanMixing;
import ccre.supercanvas.BaseChannelComponent;

/**
 * A component allowing interaction with booleans.
 *
 * @author skeggsc
 */
public class BooleanControlComponent extends BaseChannelComponent<BooleanControlComponent.View> implements BooleanInput {

    public static enum View {
        CONFIGURATION, RED_GREEN_SWITCH, LINEAR_ON_OFF
    }

    private static final long serialVersionUID = 3529467636546288860L;
    private final BooleanStatus pressed = new BooleanStatus();
    private final BooleanOutput rawOut;
    private boolean hasSentInitial = false;

    /**
     * Create a new BooleanControlComponent with a BooleanOutput to control.
     *
     * @param cx the X coordinate.
     * @param cy the Y coordinate.
     * @param name the name of the output.
     * @param out the BooleanOutput to control.
     */
    public BooleanControlComponent(int cx, int cy, String name, BooleanOutput out) {
        super(cx, cy, name);
        rawOut = out;
    }

    /**
     * Create a new BooleanControlComponent.
     *
     * @param cx the X coordinate.
     * @param cy the Y coordinate.
     * @param name the name of the output.
     */
    public BooleanControlComponent(int cx, int cy, String name) {
        super(cx, cy, name);
        rawOut = BooleanMixing.ignoredBooleanOutput;
    }

    @Override
    protected boolean containsForInteract(int x, int y) {
        switch (activeView) {
        case RED_GREEN_SWITCH:
            return x >= centerX - 40 && x <= centerX + 30 && y >= centerY - 20 && y <= centerY + 30;
        case LINEAR_ON_OFF:
            return x >= centerX - halfWidth + 5 && x <= centerX + halfWidth - 5 && y >= centerY - 15 && y <= centerY + 15;
        default:
            return false;
        }
    }

    @Override
    public void channelRender(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        boolean isPressed = this.pressed.get();
        switch (activeView) {
        case RED_GREEN_SWITCH:
            AffineTransform origO = g.getTransform();
            {
                g.setColor(isPressed ? Color.GREEN.darker() : Color.RED.darker());
                AffineTransform orig = g.getTransform();
                g.rotate(isPressed ? 10 : -10, centerX + (isPressed ? 3 : -3), centerY + 10);
                g.fillRect(centerX - 5, centerY + 5, 10, 45);
                g.setTransform(orig);
                g.setColor(Color.GRAY.darker().darker());
                g.fillRect(centerX - 20, centerY + 10, 40, 20);
            }
            g.translate(-5, 2);
            {
                g.setColor(isPressed ? Color.GREEN : Color.RED);
                AffineTransform orig = g.getTransform();
                g.rotate(isPressed ? 10 : -10, centerX + (isPressed ? 3 : -3), centerY + 10);
                g.fillRect(centerX - 5, centerY + 5, 10, 45);
                g.setTransform(orig);
                g.setColor(Color.GRAY.darker());
                g.fillRect(centerX - 20, centerY + 10, 40, 20);
            }
            g.setTransform(origO);
            break;
        case LINEAR_ON_OFF:
            g.setColor(Color.LIGHT_GRAY);
            g.fillRoundRect(centerX - halfWidth + 10, centerY - 20, halfWidth * 2 - 20, 40, 20, 20);
            g.setColor(Color.BLACK);
            g.drawRoundRect(centerX - halfWidth + 10, centerY - 20, halfWidth * 2 - 20, 40, 20, 20);
            g.setColor(isPressed ? Color.GREEN : Color.RED);
            g.fillRoundRect(centerX - halfWidth + (isPressed ? halfWidth + 5 : 15), centerY - 15, halfWidth - 20, 30, 20, 20);
            g.setColor(Color.BLACK);
            g.drawRoundRect(centerX - halfWidth + (isPressed ? halfWidth + 5 : 15), centerY - 15, halfWidth - 20, 30, 20, 20);
            Stroke oldStroke = g.getStroke();
            g.setStroke(new BasicStroke(3, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));
            g.setColor(isPressed ? Color.BLACK : Color.GRAY);
            g.drawLine(centerX + halfWidth / 2 - 5, centerY - 5, centerX + halfWidth / 2 - 5, centerY + 5);
            g.setColor(!isPressed ? Color.BLACK : Color.GRAY);
            g.drawOval(centerX - halfWidth / 2, centerY - 5, 10, 10);
            g.setStroke(oldStroke);
            break;
        case CONFIGURATION: // never called
        }
    }

    @Override
    public boolean onInteract(int x, int y) {
        if (!containsForInteract(x, y)) {
            return false;
        }
        switch (activeView) {
        case RED_GREEN_SWITCH:
            pressed.set(!pressed.get());
            if (!hasSentInitial && rawOut != null) {
                pressed.send(rawOut);
                hasSentInitial = true;
            }
            return true;
        case LINEAR_ON_OFF:
            if (x < centerX - 5) {
                pressed.set(false);
            } else if (x > centerX + 5) {
                pressed.set(true);
            }
            return true;
        default:
            return false;
        }
    }

    @Override
    public void send(BooleanOutput output) {
        pressed.send(output);
    }

    @Override
    public void unsend(BooleanOutput output) {
        pressed.unsend(output);
    }

    @Override
    public boolean get() {
        return pressed.get();
    }

    @Override
    protected void setDefaultView() {
        activeView = View.RED_GREEN_SWITCH;
    }
}
