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
package ccre.supercanvas.components.channels;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.io.Serializable;

import ccre.channel.BooleanInput;
import ccre.channel.BooleanOutput;
import ccre.channel.EventOutput;
import ccre.rconf.RConf.Entry;
import ccre.supercanvas.BaseChannelComponent;
import ccre.supercanvas.Rendering;
import ccre.supercanvas.SuperCanvasPanel;

/**
 * A component allowing interaction with booleans.
 *
 * @author skeggsc
 */
public class BooleanControlComponent extends BaseChannelComponent<BooleanControlComponent.View> {

    private static final long serialVersionUID = -5140494090957643875L;

    static enum View {
        RED_GREEN_SWITCH, LINEAR_ON_OFF, TEXTUAL
    }

    private boolean lastSentValue;
    private final BooleanInput alternateSource;
    private final BooleanOutput rawOut;
    private EventOutput unsubscribe;

    /**
     * Create a new BooleanControlComponent with a BooleanOutput to control.
     *
     * @param cx the X coordinate.
     * @param cy the Y coordinate.
     * @param name the name of the output.
     * @param out the BooleanOutput to control.
     */
    public BooleanControlComponent(int cx, int cy, String name, BooleanOutput out) {
        this(cx, cy, name, null, out);
    }

    /**
     * Create a new BooleanControlComponent.
     *
     * @param cx the X coordinate.
     * @param cy the Y coordinate.
     * @param name the name of the output.
     */
    public BooleanControlComponent(int cx, int cy, String name) {
        this(cx, cy, name, BooleanOutput.ignored);
    }

    /**
     * Create a new BooleanControlComponent, with an input channel to represent
     * the actual value as returned by the remote.
     *
     * @param cx the X coordinate.
     * @param cy the Y coordinate.
     * @param name the name of the output.
     * @param inp the BooleanInput to control.
     * @param out the BooleanOutput to control.
     */
    public BooleanControlComponent(int cx, int cy, String name, BooleanInput inp, BooleanOutput out) {
        super(cx, cy, name);
        rawOut = out;
        alternateSource = inp;
    }

    @Override
    protected boolean containsForInteract(int x, int y) {
        switch (activeView) {
        case RED_GREEN_SWITCH:
            return x >= centerX - 40 && x <= centerX + 30 && y >= centerY - 20 && y <= centerY + 30;
        case LINEAR_ON_OFF:
            return x >= centerX - halfWidth + 5 && x <= centerX + halfWidth - 5 && y >= centerY - 15 && y <= centerY + 15;
        case TEXTUAL:
            return x >= centerX - 50 && x <= centerX + 50 && y >= centerY - 10 && y <= centerY + 20;
        default:
            return false;
        }
    }

    private boolean getDele() {
        return this.alternateSource != null ? this.alternateSource.get() : lastSentValue;
    }

    @Override
    public void channelRender(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        boolean isPressed = getDele();
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
        case TEXTUAL:
            g.setFont(Rendering.labels);
            if (getPanel().editmode) {
                g.setColor(Color.BLACK);
            } else {
                g.setColor(isPressed ? Color.GREEN : Color.RED);
            }
            String text = isPressed ? "TRUE" : "FALSE";
            g.drawString(text, centerX - g.getFontMetrics().stringWidth(text) / 2, centerY + g.getFontMetrics().getAscent() / 2);
            break;
        }
    }

    @Override
    public boolean onInteract(int x, int y) {
        if (!containsForInteract(x, y)) {
            return false;
        }
        switch (activeView) {
        case RED_GREEN_SWITCH:
        case TEXTUAL:
            lastSentValue = !getDele();
            break;
        case LINEAR_ON_OFF:
            if (x < centerX - 5) {
                lastSentValue = false;
            } else if (x > centerX + 5) {
                lastSentValue = true;
            } else {
                return true;
            }
            break;
        default:
            return false;
        }
        if (rawOut != null) {
            rawOut.set(lastSentValue);
        }
        return true;
    }

    @Override
    protected void setDefaultView() {
        activeView = View.RED_GREEN_SWITCH;
    }

    private final BooleanOutput fakeOut = new FakeBooleanOutput();
    private boolean isFakeSubscribed = false;

    @Override
    protected void onChangePanel(SuperCanvasPanel panel) {
        boolean hasPanel = panel != null;
        if (alternateSource != null && hasPanel != isFakeSubscribed) {
            if (unsubscribe != null) {
                unsubscribe.event();
                unsubscribe = null;
            }
            if (hasPanel) {
                unsubscribe = alternateSource.sendR(fakeOut);
            }
            isFakeSubscribed = hasPanel;
        }
    }

    private static final class FakeBooleanOutput implements BooleanOutput, Serializable {
        private static final long serialVersionUID = -5025143910878910655L;

        @Override
        public void set(boolean b) {
            // Do nothing. This is just so that we can make the remote end send us data by subscribing.
        }
    }

    @Override
    public Entry[] queryRConf() throws InterruptedException {
        return rconfBase();
    }

    @Override
    public boolean signalRConf(int field, byte[] data) throws InterruptedException {
        return rconfBase(field, data) == BASE_VALID;
    }
}
