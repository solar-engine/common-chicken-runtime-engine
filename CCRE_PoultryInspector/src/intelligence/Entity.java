/*
 * Copyright 2013 Colby Skeggs
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
package intelligence;

import ccre.cluck.CluckNode;
import static ccre.cluck.CluckNode.RMT_BOOLOUTP;
import static ccre.cluck.CluckNode.RMT_BOOLPROD;
import static ccre.cluck.CluckNode.RMT_EVENTCONSUMER;
import static ccre.cluck.CluckNode.RMT_EVENTSOURCE;
import static ccre.cluck.CluckNode.RMT_FLOATOUTP;
import static ccre.cluck.CluckNode.RMT_FLOATPROD;
import static ccre.cluck.CluckNode.RMT_LOGTARGET;
import static ccre.cluck.CluckNode.RMT_OUTSTREAM;
import ccre.event.EventConsumer;
import ccre.event.EventSource;
import ccre.log.Logger;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;

public class Entity {

    public final Remote represented;
    public int centerX, centerY;
    protected boolean registered;
    protected long countStart = 0;
    protected int width = 20, height = 20;

    public Entity(Remote remote, int centerX, int centerY) {
        this.represented = remote;
        this.centerX = centerX;
        this.centerY = centerY;
    }

    public void render(Graphics g) {
        g.setFont(IntelligenceMain.console);
        FontMetrics fm = g.getFontMetrics();
        int w = 70, h = fm.getHeight() * 3 / 2;
        width = w;
        height = h;
        g.setColor(Color.BLACK);
        g.fillRect(centerX - w, centerY - h, w * 2, h * 2);
        Color col = represented.getColor();
        g.setColor(col);
        g.fillRect(centerX - w + 1, centerY - h + 1, w * 2 - 2, h * 2 - 2);
        g.setColor(IntelligenceMain.foreground);
        g.drawString(represented.remote, centerX - w + 1, centerY - h - 1 + g.getFontMetrics().getAscent());
        g.drawString(CluckNode.rmtToString(represented.type), centerX - w + 1, centerY - h - 1 + g.getFontMetrics().getAscent() + g.getFontMetrics().getHeight());
        represented.checkout();
        Object co = represented.checkout;
        if (co == null) {
            return;
        }
        long count = (System.currentTimeMillis() - countStart);
        int rh = fm.getHeight();
        switch (represented.type) {
            case RMT_EVENTCONSUMER:
                EventConsumer ec = (EventConsumer) co;
                g.setColor(blend(col.darker(), col, count / 500.0f));
                g.fillRect(centerX - w + 1, centerY + h - rh - 1, w * 2 - 2, rh - 2);
                break;
            case RMT_EVENTSOURCE:
                EventSource es = (EventSource) co;
                if (!registered) {
                    registered = true;
                    es.addListener(new EventConsumer() {
                        @Override
                        public void eventFired() {
                            countStart = System.currentTimeMillis();
                        }
                    });
                }
                g.setColor(blend(col.darker(), col, count / 500.0f));
                g.fillRect(centerX - w + 1, centerY + h - rh - 1, w * 2 - 2, rh - 2);
                break;
            case RMT_LOGTARGET:

                break;
            case RMT_BOOLPROD:

                break;
            case RMT_BOOLOUTP:

                break;
            case RMT_FLOATPROD:

                break;
            case RMT_FLOATOUTP:

                break;
            case RMT_OUTSTREAM:

                break;
        }
    }

    public boolean isOver(Point point) {
        return Math.abs(point.getX() - centerX) <= width && Math.abs(point.getY() - centerY) <= height;
    }

    public static Color blend(Color a, Color b, float f) {
        float bp;
        if (f < 0) {
            bp = 0;
        } else if (f > 1) {
            bp = 1;
        } else {
            bp = f;
        }
        float ap = 1 - bp;
        return new Color(Math.round(a.getRed() * ap + b.getRed() * bp), Math.round(a.getGreen() * ap + b.getGreen() * bp), Math.round(a.getBlue() * ap + b.getBlue() * bp), Math.round(a.getAlpha() * ap + b.getAlpha() * bp));
    }
}
