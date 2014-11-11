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

import ccre.channel.EventInput;
import ccre.channel.EventOutput;
import ccre.channel.EventStatus;
import ccre.supercanvas.BaseChannelComponent;
import ccre.supercanvas.Rendering;

/**
 * A component allowing interaction with events.
 *
 * @author skeggsc
 */
public class EventControlComponent extends BaseChannelComponent<EventControlComponent.View> implements EventInput {

    public static enum View {
        CONFIGURATION, ISOMETRIC_BUTTON, SQUARE_BUTTON, TEXTUAL
    }

    private static final long serialVersionUID = 5604099540525088534L;
    private transient long countStart;
    private final EventStatus stat = new EventStatus();

    /**
     * Create a new EventControlComponent with a EventOutput to control.
     *
     * @param cx the X coordinate.
     * @param cy the Y coordinate.
     * @param name the name of the output.
     * @param out the EventOutput to control.
     */
    public EventControlComponent(int cx, int cy, String name, EventOutput out) {
        this(cx, cy, name);
        stat.send(out);
    }

    /**
     * Create a new EventControlComponent.
     *
     * @param cx the X coordinate.
     * @param cy the Y coordinate.
     * @param name the name of the output.
     */
    public EventControlComponent(int cx, int cy, String name) {
        super(cx, cy, name);
    }

    private transient int clickWidth;
    
    @Override
    protected boolean containsForInteract(int x, int y) {
        switch (activeView) {
        case ISOMETRIC_BUTTON:
            return x >= centerX - halfWidth / 3 - 10 && x <= centerX + halfWidth / 3 + 10 && y >= centerY - halfHeight / 3 - 10 && y <= centerY + halfHeight / 3 + 20;
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
            g.fillOval(centerX - halfWidth / 3, 10 + centerY - halfHeight / 3, 2 * halfWidth / 3, 2 * halfHeight / 3);
            g.fillRect(centerX - halfWidth / 3 + 1, 10 + centerY - rel, 2 * halfWidth / 3 - 1, rel);
            g.setColor(count < 200 ? Color.GREEN : Color.RED);
            g.fillOval(centerX - halfWidth / 3, 10 + centerY - halfHeight / 3 - rel, 2 * halfWidth / 3, 2 * halfHeight / 3);
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
        case CONFIGURATION: // never called
        }
    }

    @Override
    public boolean onInteract(int x, int y) {
        stat.event();
        countStart = System.currentTimeMillis();
        return true;
    }

    @Override
    public void send(EventOutput listener) {
        stat.send(listener);
    }

    @Override
    public void unsend(EventOutput listener) {
        stat.unsend(listener);
    }

    @Override
    protected void setDefaultView() {
        activeView = View.ISOMETRIC_BUTTON;
    }
}
