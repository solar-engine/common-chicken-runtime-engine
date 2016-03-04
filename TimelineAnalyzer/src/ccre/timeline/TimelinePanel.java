/*
 * Copyright 2016 Cel Skeggs
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
package ccre.timeline;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.util.Arrays;

import javax.swing.JPanel;

import ccre.log.Logger;
import ccre.time.Time;

/**
 * A base display panel used in timeline panels.
 *
 * @author skeggsc
 */
public final class TimelinePanel extends JPanel {

    private static final long serialVersionUID = 7927046605855742517L;
    private static final int TOOLBAR_HEIGHT = 30;
    private static final int TIME_HEIGHT = 20;
    /**
     * The currently-visible channels in this timeline.
     */
    private final Timeline timeline;
    /**
     * The XY position of the upper-left corner of the view.
     */
    private int relativeX, relativeY;
    /**
     * The sizing of the view.
     */
    private float widthSeconds = 2.0f, heightChannels = 5;
    /**
     * The relative position for the last drag start.
     */
    private int relDragX, relDragY;
    private boolean dragModeScale;
    /**
     * The most recent position of the mouse.
     */
    private int mouseX, mouseY;

    /**
     * Creates a new timeline display panel.
     *
     * @param timeline the timeline to display.
     */
    public TimelinePanel(Timeline timeline) {
        this.timeline = timeline;
    }

    /**
     * Start the IntelligenceMain instance so that it runs.
     */
    public void start() {
        MouseAdapter listener = new SuperCanvasMouseAdapter();
        this.addMouseWheelListener(listener);
        this.addMouseListener(listener);
        this.addMouseMotionListener(listener);
    }

    /**
     * Console text, small and monospaced.
     */
    public static final Font console = new Font("Monospaced", Font.PLAIN, 11);
    private static final int CAP_PAD = 40;

    @Override
    public void paint(Graphics go) {
        try {
            Graphics2D g = (Graphics2D) go;
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            int w = getWidth();
            int h = getHeight();
            g.setFont(console);
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, w, h);
            renderTimeline(g, w, h);
        } catch (Throwable thr) {
            Logger.severe("Exception while handling paint event", thr);
        }
    }

    private void renderTimeline(Graphics2D g, int w, int h) {
        // sections of the screen: toolbar, top seconds bar, bottom seconds bar,
        // timeline
        g.setColor(Color.CYAN);
        g.fillRect(0, 0, w, TOOLBAR_HEIGHT);

        AffineTransform oldTX = g.getTransform();

        g.translate(0, TOOLBAR_HEIGHT);
        drawTickMarks(g, w, TIME_HEIGHT);
        g.setTransform(oldTX);

        g.translate(0, h - TIME_HEIGHT);
        drawTickMarks(g, w, TIME_HEIGHT);
        g.setTransform(oldTX);

        int nh = h - TOOLBAR_HEIGHT - TIME_HEIGHT * 2;
        Shape s = g.getClip();
        g.clipRect(0, TOOLBAR_HEIGHT + TIME_HEIGHT, w, nh);

        oldTX = g.getTransform();
        int channel_height = (int) (nh / heightChannels);
        int n = 0;
        for (TimelineChannel channel : timeline.channels) {
            int relY = TOOLBAR_HEIGHT + TIME_HEIGHT + channel_height * n - relativeY;
            g.translate(-relativeX, relY);
            renderChannel(channel, relativeX, g, w, channel_height);
            g.setTransform(oldTX);
            g.setColor(Color.RED);
            g.drawString(channel.name(), CAP_PAD, relY + 3 + g.getFontMetrics().getAscent());
            n++;
        }
        g.setClip(s);
    }

    private void drawTickMarks(Graphics2D g, int w, int h) {
        g.setColor(Color.GREEN);
        g.fillRect(0, 0, w, TIME_HEIGHT);
        double time_duration = 1.0f; // in seconds
        int time_duration_power = 0;
        double period_width = w / widthSeconds;
        while (period_width < 20) {
            period_width *= 10;
            time_duration *= 10;
            time_duration_power++;
        }
        while (period_width > 200) {
            period_width /= 10;
            time_duration /= 10;
            time_duration_power--;
        }
        float time_at_left = relativeX / (w / widthSeconds);
        float time_at_width = (relativeX + w) / (w / widthSeconds);
        // Logger.finest("Tick evaluation: " + time_duration + " ~ " +
        // period_width + " / " + time_at_left + " - " + time_at_width);
        int next_tick_at_left_od = ((int) Math.ceil(time_at_left / time_duration));
        g.setColor(Color.YELLOW);
        for (int tick_od = next_tick_at_left_od - 1; tick_od * time_duration < time_at_width; tick_od++) {
            int position = (int) (tick_od * time_duration * (w / widthSeconds)) - relativeX;
            g.drawLine(position, 1, position, h - 2);
            g.drawString(toPowerString(tick_od, time_duration_power), position + 1, h / 2);
        }
    }

    /**
     * Converts a time, in units of 10 microseconds, to a textual representation
     * that includes units and displays at most three significant digits.
     *
     * @param ticks the number of ticks, in multiples of 10 microseconds.
     * @return the string, either <code>xxx ys</code>, <code>xx.x ys</code>, or
     * <code>x.xx ys</code>, where <code>x</code> and <code>y</code> are chosen
     * by the code.
     */
    public static String toTimeString(long ticks) {
        // we want to display the three most significant digits
        // xxx ys, xx.x ys, x.xx ys
        ticks *= 10;
        long secs_orig = (ticks / Time.MICROSECONDS_PER_SECOND);
        int shift = 0;
        while (ticks >= 1000) {
            ticks /= 10;
            shift++;
        }
        char c1 = (char) ((ticks / 100) + '0');
        char c2 = (char) (((ticks / 10) % 10) + '0');
        char c3 = (char) ((ticks % 10) + '0');

        switch (shift) {
        case 0:
            return "" + c1 + c2 + c3 + " us";
        case 1:
            return "" + c1 + "." + c2 + c3 + " ms";
        case 2:
            return "" + c1 + c2 + "." + c3 + " ms";
        case 3:
            return "" + c1 + c2 + c3 + " ms";
        case 4:
            return "" + c1 + "." + c2 + c3 + " s";
        case 5:
            return "" + c1 + c2 + "." + c3 + " s";
        default:
            return secs_orig + " s";
        }
    }

    /**
     * Converts a number into a string after multiplication by
     * <code>Math.pow(10, pow10)</code>, but without any potential overflow
     * errors.
     *
     * @param num the base number.
     * @param pow10 the power of ten.
     * @return the number converted to a string.
     */
    public static String toPowerString(int num, int pow10) {
        if (num < 0) {
            return "-" + toPowerString(-num, pow10);
        }
        // tick * 10^pow10
        StringBuilder sb = new StringBuilder().append(num);
        if (pow10 >= 0) {
            while (pow10-- > 0) {
                sb.append('0');
            }
        } else {
            int count_needed = -(pow10 + sb.length());
            if (count_needed >= 0) {
                char[] zeroes = new char[count_needed + 2];
                Arrays.fill(zeroes, '0');
                zeroes[1] = '.';
                sb.insert(0, new String(zeroes));
            } else {
                sb.insert(sb.length() + pow10, '.');
            }
        }
        return sb.toString();
    }

    private void renderChannel(TimelineChannel channel, int relativeX, Graphics2D g, int w, int h) {
        int virtual_begin_at = (int) ((w / widthSeconds) * channel.beginAt()) - CAP_PAD;
        int virtual_end_at = (int) ((w / widthSeconds) * channel.endAt()) + CAP_PAD;
        g.setColor(Color.WHITE);
        g.fillRect(virtual_begin_at, 0, virtual_end_at - virtual_begin_at, h);
        FontMetrics fm = g.getFontMetrics();
        int lastLocationX = 0, lastLocationY = 0;
        for (int i = 0; i < channel.count(); i++) {
            float finc = ((w / widthSeconds) * channel.timeFor(i));
            if (finc > relativeX + w * 2 || finc < relativeX - w) {
                continue;
            }
            int virtual_incidence = (int) finc;

            g.setColor(channel.colorFor(i));
            g.drawLine(virtual_incidence, channel.isFloat() ? (h - h / 3) : 1, virtual_incidence, h - 2);

            if (i != 0) {
                g.setColor(channel.colorFor(i - 1));
                if (channel.isFloat()) {
                    int vloc = (int) (h / 2 - (h / 2) * channel.valueFor(i));
                    g.drawLine(lastLocationX, lastLocationY, virtual_incidence, vloc);
                    lastLocationY = vloc;
                } else if (channel.hasContinuationChannel()) {
                    g.drawLine(lastLocationX, h / 2, virtual_incidence, h / 2);
                    g.drawLine(Math.max(lastLocationX, virtual_incidence - 1), 1, Math.max(lastLocationX, virtual_incidence - 1), h - 2);
                }
            }

            String text = channel.stringFor(i);
            int next_virtual_incidence = (i < channel.count() - 1) ? (int) ((w / widthSeconds) * channel.timeFor(i + 1)) : Integer.MAX_VALUE;
            int width_available_for_text = next_virtual_incidence - (4 + virtual_incidence);
            if (width_available_for_text > fm.stringWidth("M")) {
                g.setColor(channel.colorFor(i));
                while (fm.stringWidth(text) > width_available_for_text) {
                    text = text.substring(0, text.length() - 1);
                }
                if (!text.isEmpty()) {
                    g.drawString(text, virtual_incidence + 2, h / 2);
                }
            }

            lastLocationX = virtual_incidence;
        }
    }

    private class SuperCanvasMouseAdapter extends MouseAdapter {

        SuperCanvasMouseAdapter() {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            try {
                dragModeScale = e.isShiftDown();
                if (dragModeScale) {
                    relDragX = e.getX();
                    relDragY = e.getY();
                } else {
                    relDragX = relativeX + e.getX();
                    relDragY = relativeY + e.getY();
                }
                repaint();
            } catch (Throwable thr) {
                Logger.severe("Exception while handling mouse press", thr);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            try {
                mouseX = e.getX();
                mouseY = e.getY();
                updateDrag();
            } catch (Throwable thr) {
                Logger.severe("Exception while handling mouse release", thr);
            }
        }

        private void updateDrag() {
            if (dragModeScale) {
                if (mouseX - relDragX < -30) {
                    relDragX -= 30;
                    float centerTime = (relativeX + (getWidth() / 2)) * (widthSeconds / getWidth());
                    widthSeconds *= 2;
                    relativeX = (int) (centerTime / (widthSeconds / getWidth()) - (getWidth() / 2));
                } else if (mouseX - relDragX > 30) {
                    relDragX += 30;
                    float centerTime = (relativeX + (getWidth() / 2)) * (widthSeconds / getWidth());
                    widthSeconds /= 2;
                    relativeX = (int) (centerTime / (widthSeconds / getWidth()) - (getWidth() / 2));
                }
                if (mouseY - relDragY < -30) {
                    relDragY -= 30;
                    float centerTime = (relativeY + (getHeight() / 2)) * (heightChannels / getHeight());
                    heightChannels *= 2;
                    relativeY = (int) (centerTime / (heightChannels / getHeight()) - (getHeight() / 2));
                } else if (mouseY - relDragY > 30) {
                    relDragY += 30;
                    float centerTime = (relativeY + (getHeight() / 2)) * (heightChannels / getHeight());
                    heightChannels /= 2;
                    relativeY = (int) (centerTime / (heightChannels / getHeight()) - (getHeight() / 2));
                }
            } else {
                relativeX = relDragX - mouseX;
                relativeY = relDragY - mouseY;
            }
            repaint();
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            try {
                // TODO
                repaint();
            } catch (Throwable thr) {
                Logger.severe("Exception while handling mouse wheel", thr);
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            try {
                mouseX = e.getX();
                mouseY = e.getY();
                updateDrag();
            } catch (Throwable thr) {
                Logger.severe("Exception while handling mouse drag", thr);
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            try {
                mouseX = e.getX();
                mouseY = e.getY();
                // repaint();
            } catch (Throwable thr) {
                Logger.severe("Exception while handling mouse move", thr);
            }
        }
    }
}
