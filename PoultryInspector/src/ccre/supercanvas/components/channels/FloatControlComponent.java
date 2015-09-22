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

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.channel.EventOutput;
import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.rconf.RConf;
import ccre.rconf.RConf.Entry;
import ccre.supercanvas.BaseChannelComponent;
import ccre.supercanvas.Rendering;
import ccre.supercanvas.SuperCanvasPanel;

/**
 * A component allowing interaction with floats.
 *
 * @author skeggsc
 */
public class FloatControlComponent extends BaseChannelComponent<FloatControlComponent.View> {
    private static Logger logger = LoggerFactory.getLogger(FloatControlComponent.class);

    private static final long serialVersionUID = -5862659067200938010L;

    static enum View {
        HORIZONTAL_POINTER, TICKER, TEXTUAL
    }

    private float lastSentValue;
    private final FloatInput alternateSource;
    private final FloatOutput rawOut;
    private float minimum = -1.0f, maximum = 1.0f;
    private boolean hasSentInitial = false;
    private StringBuilder activeBuffer;
    private EventOutput unsubscribe;

    /**
     * Create a new FloatControlComponent with a FloatOutput to control.
     *
     * @param cx the X coordinate.
     * @param cy the Y coordinate.
     * @param name the name of the output.
     * @param out the FloatOutput to control.
     */
    public FloatControlComponent(int cx, int cy, String name, FloatOutput out) {
        this(cx, cy, name, null, out);
    }

    /**
     * Create a new FloatControlComponent, with an input channel to represent
     * the actual value as returned by the remote.
     *
     * @param cx the X coordinate.
     * @param cy the Y coordinate.
     * @param name the name of the output.
     * @param inp the FloatInput to monitor.
     * @param out the FloatOutput to control.
     */
    public FloatControlComponent(int cx, int cy, String name, FloatInput inp, FloatOutput out) {
        super(cx, cy, name);
        rawOut = out;
        alternateSource = inp;
    }

    /**
     * Create a new FloatControlComponent.
     *
     * @param cx the X coordinate.
     * @param cy the Y coordinate.
     * @param name the name of the output.
     */
    public FloatControlComponent(int cx, int cy, String name) {
        this(cx, cy, name, FloatOutput.ignored);
    }

    @Override
    protected boolean containsForInteract(int x, int y) {
        switch (activeView) {
        case HORIZONTAL_POINTER:
            return x >= centerX - halfWidth + 10 && x <= centerX + halfWidth - 10 && y >= centerY - halfHeight / 2 && y <= centerY + halfHeight / 2;
        case TEXTUAL:
            return y >= centerY - 5 && y <= centerY + 10;
        case TICKER:
            for (int i = 0; i < 6; i++) {
                if (mouseInBox(i, x, y)) {
                    return true;
                }
            }
            return false;
        default:
            return false;
        }
    }

    @Override
    public void channelRender(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        if (activeView != View.TEXTUAL) {
            if (getPanel().editing == activeBuffer) {
                getPanel().editing = null;
            }
            activeBuffer = null;
        }
        boolean hasValue = alternateSource != null || this.hasSentInitial;
        switch (activeView) {
        case HORIZONTAL_POINTER:
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
            if (hasValue) {
                float value = getDele();
                int ptrCtr = (int) (centerX + halfWidth * ((2 * (value - minimum) / (maximum - minimum)) - 1) * 2 / 3);
                if (value < 0) {
                    g.setColor(value == -1 ? Color.RED : Color.RED.darker().darker());
                } else if (value > 0) {
                    g.setColor(value == 1 ? Color.GREEN : Color.GREEN.darker().darker());
                } else {
                    g.setColor(Color.ORANGE);
                }
                Shape c = g.getClip();
                g.setClip(new Rectangle(centerX - halfWidth + 10, centerY - halfHeight / 2, halfWidth * 2 - 20, halfHeight));
                g.drawPolygon(new int[] { ptrCtr - 12, ptrCtr - 8, ptrCtr - 12 }, new int[] { centerY - 8, centerY - 4, centerY }, 3);
                g.drawPolygon(new int[] { ptrCtr + 12, ptrCtr + 8, ptrCtr + 12 }, new int[] { centerY - 8, centerY - 4, centerY }, 3);
                g.fillRect(ptrCtr - 5, centerY - halfHeight / 2 + 1, 11, halfHeight / 2 - 4);
                g.fillPolygon(new int[] { ptrCtr - 5, ptrCtr, ptrCtr + 6 }, new int[] { centerY - 3, centerY + 3, centerY - 3 }, 3);
                g.setClip(c);
            }
            break;
        case TICKER:
            g.setColor(Color.BLACK);
            g.setFont(Rendering.labels);
            fontMetrics = g.getFontMetrics();
            String text = hasValue ? String.format("%.2f", getDele()) : "????";
            g.drawString(text, centerX - fontMetrics.stringWidth(text) / 2, centerY + fontMetrics.getDescent());
            paintBox(g, fontMetrics, mouseX, mouseY, true, 0);
            paintBox(g, fontMetrics, mouseX, mouseY, true, 1);
            paintBox(g, fontMetrics, mouseX, mouseY, true, 2);
            paintBox(g, fontMetrics, mouseX, mouseY, false, 0);
            paintBox(g, fontMetrics, mouseX, mouseY, false, 1);
            paintBox(g, fontMetrics, mouseX, mouseY, false, 2);
            break;
        case TEXTUAL:
            g.setFont(Rendering.labels);
            String default_ = hasValue ? Float.toString(getDele()) : "?";
            if (activeBuffer == null) {
                activeBuffer = new StringBuilder(default_);
            }
            if (getPanel().editing != activeBuffer && !activeBuffer.toString().equals(default_)) {
                activeBuffer.setLength(0);
                activeBuffer.append(default_);
            }
            g.setColor(getPanel().editing == activeBuffer ? Color.RED : Color.BLACK);
            if (g.getFontMetrics().stringWidth(activeBuffer.toString()) > 2 * halfWidth) {
                g.setFont(Rendering.console);
            }
            g.drawString(activeBuffer.toString(), centerX - g.getFontMetrics().stringWidth(activeBuffer.toString()) / 2, centerY + 5);
            break;
        }
    }

    private int[] boundingBoxes;

    private boolean mouseInBox(int boxId, int mouseX, int mouseY) {
        return boundingBoxes[boxId * 4 + 0] <= mouseX && mouseX < boundingBoxes[boxId * 4 + 1] &&
                boundingBoxes[boxId * 4 + 2] <= mouseY && mouseY < boundingBoxes[boxId * 4 + 3];
    }

    private void paintBox(Graphics g, FontMetrics fontMetrics, int mouseX, int mouseY, boolean isRight, int rowId) {
        int left = isRight ? centerX + halfWidth - fontMetrics.stringWidth("+") : centerX - halfWidth;
        int right = left + fontMetrics.stringWidth(isRight ? "+" : "-");
        int bottom = centerY + rowId * (fontMetrics.getHeight()) / 2 + (getPanel().editmode ? 0 : -10);
        int top = bottom - fontMetrics.getHeight() / 2;

        if (boundingBoxes == null) {
            boundingBoxes = new int[6 * 4];
        }

        int boxIndex = rowId + (isRight ? 3 : 0);
        boundingBoxes[4 * boxIndex + 0] = left;
        boundingBoxes[4 * boxIndex + 1] = right;
        boundingBoxes[4 * boxIndex + 2] = top;
        boundingBoxes[4 * boxIndex + 3] = bottom;
        g.setColor(mouseInBox(boxIndex, mouseX, mouseY) ? Color.BLACK : Color.GRAY);
        g.drawString(isRight ? "+" : "-", left, bottom);
    }

    @Override
    public boolean wantsDragSelect() {
        return true;
    }

    @Override
    public void onPressedEnter() {
        if (activeView == View.TEXTUAL && getPanel().editing == activeBuffer) {
            try {
                setDele(false, Float.parseFloat(activeBuffer.toString()));
                getPanel().editing = null;
            } catch (NumberFormatException ex) {
                logger.warn("Could not parse number '" + activeBuffer + "'.");
            }
        }
    }

    private float getDele() {
        // Checks null in case unserialized from old version
        return alternateSource == null ? lastSentValue : alternateSource.get();
    }

    private void setDele(boolean requireDifferent, float value) {
        if (!(requireDifferent && value == getDele() && hasSentInitial)) {
            lastSentValue = value;
            if (rawOut != null) {
                rawOut.set(value);
                hasSentInitial = true;
            }
        }
    }

    @Override
    public boolean onInteract(int x, int y) {
        if (isBeingDragged()) {
            return false;
        }
        float value;
        switch (activeView) {
        case HORIZONTAL_POINTER:
            value = (x - centerX - 1) / (halfWidth * 2 / 3f);
            value = minimum + ((value + 1) / 2) * (maximum - minimum); // min to max, incl
            value = Math.min(maximum, Math.max(minimum, value));
            if (-0.1 < value && value < 0.1) {
                value = 0;
            }
            break;
        case TEXTUAL:
            getPanel().editing = (getPanel().editing == activeBuffer) ? null : activeBuffer;
            return true;
        case TICKER:
            value = getDele();
            for (int i = 0; i < 6; i++) {
                if (mouseInBox(i, x, y)) {
                    if (i < 3) {
                        value -= 0.1 * Math.pow(10, -1 + i);
                    } else {
                        value += 0.1 * Math.pow(10, -4 + i);
                    }
                    break;
                }
            }
            value = Math.round(value * 100) / 100f;
            break;
        default:
            return false;
        }
        setDele(true, value);
        return true;
    }

    @Override
    public boolean canDragInteract() {
        return true;
    }

    @Override
    protected void setDefaultView() {
        activeView = View.HORIZONTAL_POINTER;
    }

    private final FloatOutput fakeOut = new FakeFloatOutput();
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

    private static final class FakeFloatOutput implements FloatOutput, Serializable {
        private static final long serialVersionUID = 8588017785288111886L;

        @Override
        public void set(float f) {
            // Do nothing. This is just so that we can make the remote end send us data by subscribing.
        }
    }

    @Override
    public Entry[] queryRConf() throws InterruptedException {
        return rconfBase(RConf.string("minimum"), RConf.fieldFloat(minimum), RConf.string("maximum"), RConf.fieldFloat(maximum));
    }

    @Override
    public boolean signalRConf(int field, byte[] data) throws InterruptedException {
        switch (rconfBase(field, data)) {
        case 1:
            minimum = RConf.bytesToFloat(data);
            return true;
        case 3:
            maximum = RConf.bytesToFloat(data);
            return true;
        case BASE_VALID:
            return true;
        default:
            return false;
        }
    }
}
