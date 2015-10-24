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
import java.awt.Graphics2D;
import java.io.Serializable;

import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.EventCell;
import ccre.rconf.RConf.Entry;
import ccre.supercanvas.BaseChannelComponent;
import ccre.supercanvas.Rendering;
import ccre.supercanvas.SuperCanvasPanel;

/**
 * A component allowing interaction with events.
 *
 * @author skeggsc
 */
public class EventControlComponent extends BaseChannelComponent<EventControlComponent.View> {

    static enum View {
        ISOMETRIC_BUTTON, SQUARE_BUTTON, TEXTUAL
    }

    private static final long serialVersionUID = 5604099540525088534L;
    private transient long countStart;
    private final EventInput alternateSource;
    private final EventCell stat = new EventCell();
    private EventOutput unsubscribe;

    /**
     * Create a new EventControlComponent with a EventOutput to control.
     *
     * @param cx the X coordinate.
     * @param cy the Y coordinate.
     * @param name the name of the output.
     * @param out the EventOutput to control.
     */
    public EventControlComponent(int cx, int cy, String name, EventOutput out) {
        this(cx, cy, name, null, out);
    }

    /**
     * Create a new EventControlComponent.
     *
     * @param cx the X coordinate.
     * @param cy the Y coordinate.
     * @param name the name of the output.
     */
    public EventControlComponent(int cx, int cy, String name) {
        this(cx, cy, name, null);
    }

    /**
     * Create a new EventControlComponent, with an input channel to represent
     * the actual value as returned by the remote.
     *
     * @param cx the X coordinate.
     * @param cy the Y coordinate.
     * @param name the name of the output.
     * @param inp the EventInput to control.
     * @param out the EventOutput to control.
     */
    public EventControlComponent(int cx, int cy, String name, EventInput inp, EventOutput out) {
        super(cx, cy, name);
        if (out != null) {
            stat.send(out);
        }
        alternateSource = inp;
        (inp != null ? inp : stat).send(new CountNotifier());
    }

    private transient int clickWidth;

    @Override
    protected boolean containsForInteract(int x, int y) {
        switch (activeView) {
        case ISOMETRIC_BUTTON:
            return x >= centerX - halfHeight / 2 - 10 && x <= centerX + halfHeight / 2 + 10 && y >= centerY - halfHeight / 3 - 10 && y <= centerY + halfHeight / 3 + 20;
        case SQUARE_BUTTON:
            int rad = Math.min(halfWidth / 2, halfHeight / 2);
            return x >= centerX - rad && x <= centerX + rad && y >= centerY - rad - 10 && y <= centerY + rad;
        case TEXTUAL:
            return x >= centerX - clickWidth / 2 && x <= centerX + clickWidth / 2 && y >= centerY - 10 && y <= centerY + 20;
        default:
            return false;
        }
    }

    @Override
    public void channelRender(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        long count = (System.currentTimeMillis() - countStart);
        switch (activeView) {
        case ISOMETRIC_BUTTON:
            g.setColor(Color.ORANGE.darker());
            int rel = count < 200 ? 3 : 10;
            g.fillOval(centerX - halfHeight / 2, 10 + centerY - halfHeight / 3, halfHeight, 2 * halfHeight / 3);
            g.fillRect(centerX - halfHeight / 2 + 1, 10 + centerY - rel, halfHeight - 1, rel);
            g.setColor(count < 200 ? Color.GREEN : Color.RED);
            g.fillOval(centerX - halfHeight / 2, 10 + centerY - halfHeight / 3 - rel, halfHeight, 2 * halfHeight / 3);
            break;
        case SQUARE_BUTTON:
            int rad = Math.min(halfWidth / 2, halfHeight / 2);
            g.setColor(Color.BLACK);
            g.fillRect(centerX - rad, centerY - rad, rad * 2, rad * 2);
            int trad = Math.round(rad * (1 - Math.min(1, Math.max(count / 500.0f, 0))));
            g.setColor(Rendering.blend(Color.GREEN, Color.BLACK, count / 500.0f));
            g.fillRect(centerX - trad, centerY - trad, trad * 2, trad * 2);
            break;
        case TEXTUAL:
            g.setFont(Rendering.labels);
            g.setColor(Color.BLACK);
            String text = count < 500 ? "CLICKED" : "CLICK";
            clickWidth = g.getFontMetrics().stringWidth(text);
            g.drawString(text, centerX - g.getFontMetrics().stringWidth(text) / 2, centerY + g.getFontMetrics().getAscent() / 2);
            break;
        }
    }

    @Override
    public boolean onInteract(int x, int y) {
        stat.safeEvent();
        return true;
    }

    @Override
    protected void setDefaultView() {
        activeView = View.ISOMETRIC_BUTTON;
    }

    private final EventOutput fakeOut = new FakeEventOutput();
    private boolean isFakeSubscribed = false;

    @Override
    protected void onChangePanel(SuperCanvasPanel panel) {
        boolean hasPanel = panel != null;
        if (alternateSource != null && hasPanel != isFakeSubscribed) {
            if (unsubscribe != null) {
                unsubscribe.safeEvent();
                unsubscribe = null;
            }
            if (hasPanel) {
                unsubscribe = alternateSource.sendR(fakeOut);
            }
            isFakeSubscribed = hasPanel;
        }
    }

    private static final class FakeEventOutput implements EventOutput, Serializable {
        private static final long serialVersionUID = 1493349644760515921L;

        @Override
        public void event() {
            // Do nothing. This is just so that we can make the remote end send us data by subscribing.
        }
    }

    private final class CountNotifier implements EventOutput, Serializable {
        private static final long serialVersionUID = 2028623211384850963L;

        @Override
        public void event() {
            countStart = System.currentTimeMillis();
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

    public EventInput asInput() {
        return stat;
    }
}
