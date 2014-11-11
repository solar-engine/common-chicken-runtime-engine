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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import ccre.channel.FloatInput;
import ccre.channel.FloatOutput;
import ccre.log.Logger;
import ccre.supercanvas.BaseChannelComponent;
import ccre.supercanvas.Rendering;
import ccre.supercanvas.SuperCanvasPanel;

/**
 * A component allowing display of floats.
 *
 * @author skeggsc
 */
public class FloatDisplayComponent extends BaseChannelComponent<FloatDisplayComponent.View> implements FloatOutput {

    public static enum View {
        CONFIGURATION, HORIZONTAL_POINTER, DIAL, TEXTUAL
    }

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
        switch (activeView) {
        case HORIZONTAL_POINTER:
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
                String strv = String.format("%.3f", value);
                g.drawString(strv, value > 0 ? centerX - fontMetrics.stringWidth(strv) - 10 : centerX + 10, centerY - halfHeight / 2 + fontMetrics.getHeight());
            }
            int ptrCtr = centerX + (int) (halfWidth * 2 / 3 * value);
            if (Math.abs(value) <= 1) {
                if (value < 0) {
                    g.setColor(value == -1 ? Color.RED : Color.RED.darker().darker());
                } else if (value > 0) {
                    g.setColor(value == 1 ? Color.GREEN : Color.GREEN.darker().darker());
                } else {
                    g.setColor(Color.ORANGE);
                }
            }
            Shape c = g.getClip();
            g.setClip(new Rectangle(centerX - halfWidth + 10, centerY - halfHeight / 2, halfWidth * 2 - 20, halfHeight));
            g.fillRect(ptrCtr - 5, centerY - halfHeight / 2 + 1, 11, halfHeight / 2 - 4);
            g.fillPolygon(new int[] { ptrCtr - 5, ptrCtr, ptrCtr + 6 }, new int[] { centerY - 3, centerY + 3, centerY - 3 }, 3);
            g.setClip(c);
            break;
        case DIAL:
            g.setColor(Color.WHITE);
            int rad = halfWidth - 8;
            g.fillOval(centerX - halfWidth + 8, centerY - halfHeight + 20, rad * 2, rad * 2);
            g.setColor(Color.BLACK);
            g.drawOval(centerX - halfWidth + 8, centerY - halfHeight + 20, rad * 2, rad * 2);
            g.setColor(Color.BLACK);
            AffineTransform origT = g.getTransform();
            g.translate(centerX, centerY - halfHeight + 20 + rad);
            AffineTransform baseT = g.getTransform();
            int eachSpoke = 14;
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            for (int i=-10 * eachSpoke; i<=10 * eachSpoke; i += eachSpoke) {
                g.setTransform(baseT);
                g.rotate(Math.toRadians(i));
                g.translate(0, 1-rad);
                if (i % (eachSpoke * 5) == 0) {
                    g.drawLine(0, 0, 0, rad/3);
                    g.translate(0, rad / 3f);
                    g.rotate(Math.toRadians(i > 0 ? -90 : 90));
                    String str = Float.toString(i / (10f * eachSpoke));
                    g.drawString(str, i > 0 ? -g.getFontMetrics().stringWidth(str) : 0, g.getFontMetrics().getDescent());
                } else {
                    g.drawLine(0, 0, 0, rad/6);
                }
            }
            g.setTransform(baseT);
            float angle = Math.max(-170, Math.min(value * 10 * eachSpoke, 170));
            g.setColor(Color.BLUE);
            if (angle <= -170 || angle >= 170) {
                g.setColor(Color.RED);
            }
            g.rotate(Math.toRadians(angle));
            Stroke origS = g.getStroke();
            g.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.CAP_SQUARE));
            g.drawLine(0, -rad/2, 0, 0);
            g.setStroke(origS);
            g.setTransform(origT);
            break;
        case TEXTUAL:
            g.setColor(Color.BLACK);
            g.setFont(Rendering.labels);
            String text;
            if (value < 10 && value != 0) {
                text = Float.toString(value);
                String orig = text;
                while (text.length() > 3 && g.getFontMetrics().stringWidth(text) > 2 * halfWidth) {
                    String nline = text.substring(0, text.length() - 1);
                    try {
                        if (Float.parseFloat(nline) == 0) {
                            break;
                        }
                    } catch (NumberFormatException ex) {
                        Logger.warning("Unexpected failure of number formatting", ex);
                        break;
                    }
                    text = nline;
                }
                if (g.getFontMetrics().stringWidth(text) > 2 * halfWidth) {
                    g.setFont(Rendering.console);
                    text = orig;
                }
            } else {
                text = String.format("%.3f", value);
            }
            g.drawString(text, centerX - g.getFontMetrics().stringWidth(text) / 2, centerY + 5);
            break;
        case CONFIGURATION: // never called
        }
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

    @Override
    protected void setDefaultView() {
        activeView = View.HORIZONTAL_POINTER;
    }
}
