/*
 * Copyright 2013-2014 Colby Skeggs
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

import ccre.chan.*;
import ccre.cluck.CluckNode;
import static ccre.cluck.CluckNode.*;
import ccre.event.EventConsumer;
import ccre.event.EventSource;
import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.log.LoggingTarget;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JOptionPane;

/**
 * A block on the screen representing a remote target.
 *
 * @author skeggsc
 */
public final class Entity {

    /**
     * The Remote that this Entity displays.
     */
    public final Remote represented;
    /**
     * The X coordinate of the Entity on the screen.
     */
    public int centerX;
    /**
     * The Y coordinate of the Entity on the screen.
     */
    public int centerY;
    /**
     * Has this been registered so that it will be updated by the remote?
     */
    protected boolean registered;
    /**
     * When did the current animation cycle start, if an animation cycle is
     * being used?
     */
    protected long countStart = 0;
    /**
     * The current value - this depends on the kind of Remote.
     */
    protected Object currentValue;
    /**
     * The cached width of the Entity.
     */
    protected int width = 20;
    /**
     * The cached height of the Entity.
     */
    protected int height = 20;

    /**
     * Create an Entity at the specified location and using the specified
     * Remote.
     *
     * @param remote The Remote to display in this entity.
     * @param centerX The initial X position.
     * @param centerY The initial Y position.
     */
    public Entity(Remote remote, int centerX, int centerY) {
        this.represented = remote;
        this.centerX = centerX;
        this.centerY = centerY;
    }

    /**
     * Render this Entity on the specified graphics pane.
     *
     * @param g The graphics pane.
     */
    public void render(Graphics g) {
        g.setFont(IntelligenceMain.console);
        FontMetrics fm = g.getFontMetrics();
        int w = width = Math.max(70, fm.stringWidth(represented.path) / 2);
        int h = height = fm.getHeight() * 3 / 2;
        // Draw the border
        g.setColor(Color.BLACK);
        g.fillRect(centerX - w, centerY - h, w * 2, h * 2);
        // Draw the background
        Color col = represented.getColor();
        g.setColor(col);
        g.fillRect(centerX - w + 1, centerY - h + 1, w * 2 - 2, h * 2 - 2);
        // Draw the title and path
        g.setColor(IntelligenceMain.foreground);
        g.drawString(represented.path, centerX - w + 1, centerY - h - 1 + g.getFontMetrics().getAscent());
        g.drawString(CluckNode.rmtToString(represented.type), centerX - w + 1, centerY - h - 1 + g.getFontMetrics().getAscent() + g.getFontMetrics().getHeight());
        // Draw object-specific data.
        Object co = represented.checkout();
        if (co == null) {
            return;
        }
        long count = (System.currentTimeMillis() - countStart);
        int rh = fm.getHeight();
        switch (represented.type) {
            case RMT_EVENTCONSUMER:
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
            case RMT_BOOLPROD:
                BooleanInput bi = (BooleanInput) co;
                if (!registered) {
                    registered = true;
                    bi.addTarget(new BooleanOutput() {
                        @Override
                        public void writeValue(boolean value) {
                            currentValue = value;
                        }
                    });
                }
                if (currentValue != null) {
                    g.setColor((Boolean) currentValue ? Color.GREEN : Color.RED);
                    g.fillRect(centerX - w + 1, centerY + h - rh, w * 2 - 2, rh - 1);
                    g.setColor(Color.YELLOW);
                    g.drawString((Boolean) currentValue ? "TRUE" : "FALSE", centerX - w + 1, centerY + h - fm.getDescent());
                }
                break;
            case RMT_BOOLOUTP:
                g.setColor(Color.GREEN);
                g.fillRect(centerX - w + 1, centerY + h - rh, w - 1, rh - 1);
                g.setColor(Color.RED);
                g.fillRect(centerX, centerY + h - rh, w - 1, rh - 1);
                if (currentValue != null) {
                    if ((Boolean) currentValue) {
                        g.setColor(blend(Color.BLACK, Color.GREEN, count / 500.0f));
                        g.drawString("TRUE", centerX - fm.stringWidth("TRUE"), centerY + h - fm.getDescent());
                    } else {
                        g.setColor(blend(Color.BLACK, Color.RED, count / 500.0f));
                        g.drawString("FALSE", centerX, centerY + h - fm.getDescent());
                    }
                }
                break;
            case RMT_FLOATPROD:
                FloatInput fi = (FloatInput) co;
                if (!registered) {
                    registered = true;
                    fi.addTarget(new FloatOutput() {
                        @Override
                        public void writeValue(float value) {
                            currentValue = value;
                        }
                    });
                    currentValue = 0f;
                }
                float c = (Float) currentValue;
                if (c < -1) {
                    g.setColor(blend(Color.BLACK, col.darker(), c + 2));
                } else if (c > 1) {
                    g.setColor(blend(col.brighter(), Color.WHITE, c - 1));
                } else {
                    g.setColor(blend(col.darker(), col.brighter(), (c + 1) / 2));
                }
                g.fillRect(centerX - w + 1, centerY + h - rh - 1, w * 2 - 2, rh - 2);
                g.setColor(c < 0 ? Color.WHITE : Color.BLACK);
                g.drawString(String.valueOf(c), centerX - w + 1, centerY + h - fm.getDescent());
                break;
            case RMT_FLOATOUTP:
                if (currentValue != null) {
                    c = (Float) currentValue;
                    Color tcr;
                    if (c < -1) {
                        tcr = blend(Color.BLACK, col.darker(), c + 2);
                    } else if (c > 1) {
                        tcr = blend(col.brighter(), Color.WHITE, c - 1);
                    } else {
                        tcr = blend(col.darker(), col.brighter(), (c + 1) / 2);
                    }
                    g.setColor(blend(tcr, col, count / 500.0f));
                    g.fillRect(centerX - w + 1, centerY + h - rh - 1, w * 2 - 2, rh - 2);
                    g.setColor(c < 0 ? Color.WHITE : Color.BLACK);
                    g.drawString(String.valueOf(c), centerX - w + 1, centerY + h - fm.getDescent());
                }
                break;
        }
    }

    /**
     * Is the specified point on top of this block?
     *
     * @param point The point to check at.
     * @return If the point is within the bounds of the bounding shape.
     */
    public boolean isOver(Point point) {
        return Math.abs(point.getX() - centerX) <= width && Math.abs(point.getY() - centerY) <= height;
    }

    /**
     * Blend the specified colors using the specified fraction. 0 means all
     * color A, 1 means all color B, 0.5 is half-and-half, etc.
     *
     * @param a The first color.
     * @param b The second color.
     * @param f The blending factor.
     * @return The blended color.
     */
    public static Color blend(Color a, Color b, float f) {
        float bpart;
        if (f < 0) {
            bpart = 0;
        } else if (f > 1) {
            bpart = 1;
        } else {
            bpart = f;
        }
        float apart = 1 - bpart;
        return new Color(Math.round(a.getRed() * apart + b.getRed() * bpart), Math.round(a.getGreen() * apart + b.getGreen() * bpart), Math.round(a.getBlue() * apart + b.getBlue() * bpart), Math.round(a.getAlpha() * apart + b.getAlpha() * bpart));
    }

    /**
     * Interact with this Entity - this is called when it is right-clicked.
     *
     * @param x The relative mouse X.
     * @param y The relative mouse Y.
     */
    public void interact(int x, int y) {
        Object co = represented.checkout;
        if (co == null) {
            return;
        }
        switch (represented.type) {
            case RMT_EVENTCONSUMER:
                EventConsumer ec = (EventConsumer) co;
                ec.eventFired();
                countStart = System.currentTimeMillis();
                break;
            case RMT_EVENTSOURCE:
                // Interacting with this wouldn't mean anything.
                break;
            case RMT_LOGTARGET:
                String msg = JOptionPane.showInputDialog("Enter message to log", "");
                if (msg != null && !msg.isEmpty()) {
                    ((LoggingTarget) this.represented.checkout()).log(LogLevel.INFO, msg, (String) null);
                }
                break;
            case RMT_BOOLPROD:
                if (this.represented.paired.checkout() instanceof BooleanOutput) {
                    BooleanOutput bo = (BooleanOutput) this.represented.paired.checkout;
                    bo.writeValue(x < 0);
                }
                break;
            case RMT_BOOLOUTP:
                BooleanOutput bo = (BooleanOutput) co;
                boolean nw = x < 0;
                if (currentValue == null || (Boolean) currentValue != nw || System.currentTimeMillis() - countStart >= 200) {
                    bo.writeValue(nw);
                    currentValue = nw;
                    countStart = System.currentTimeMillis();
                }
                break;
            case RMT_FLOATPROD:
                if (this.represented.paired.checkout() instanceof FloatOutput) {
                    FloatOutput fo = (FloatOutput) this.represented.paired.checkout;
                    float f = x / (float) width;
                    if (y < 0) {
                        try {
                            String jop = JOptionPane.showInputDialog("Enter a number", "");
                            f = Float.parseFloat(jop);
                        } catch (NumberFormatException ex) {
                            Logger.log(LogLevel.WARNING, "Cannot write new value!", ex);
                            break;
                        }
                    }
                    fo.writeValue(f);
                }
                break;
            case RMT_FLOATOUTP:
                FloatOutput fo = (FloatOutput) co;
                float f = x / (float) width;
                if (y < 0) {
                    try {
                        String jop = JOptionPane.showInputDialog("Enter a number", "");
                        f = Float.parseFloat(jop);
                    } catch (NumberFormatException ex) {
                        Logger.log(LogLevel.WARNING, "Cannot write new value!", ex);
                        break;
                    }
                }
                fo.writeValue(f);
                currentValue = f;
                countStart = System.currentTimeMillis();
                break;
            case RMT_OUTSTREAM:
                OutputStream outs = (OutputStream) co;
                try {
                    outs.write((JOptionPane.showInputDialog("Modify value", "*") + "\n").getBytes());
                    outs.flush();
                } catch (IOException ex) {
                    Logger.log(LogLevel.WARNING, "Cannot write new value!", ex);
                }
                break;
            case RMT_INVOKE:
                Logger.info("Cannot interact with RemoteProcedures!");
                break;
        }
    }
}
