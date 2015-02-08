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

import javax.swing.JOptionPane;

import ccre.concurrency.CollapsingWorkerThread;
import ccre.log.Logger;
import ccre.rconf.RConf;
import ccre.rconf.RConfable;
import ccre.supercanvas.DraggableBoxComponent;
import ccre.supercanvas.Rendering;

/**
 * A SuperCanvas-based component to allow interaction with RConf data.
 * 
 * @author skeggsc
 */
public class RConfComponent extends DraggableBoxComponent {

    private static final long serialVersionUID = 6222627208004874042L;

    private static final Color bodyColor = Color.ORANGE;

    private RConf.Entry[] entries = new RConf.Entry[0];

    private final RConfable device;

    private final Object signalLock = new Object();
    private int signalField = -1;
    private byte[] signalPayload;
    private long lastSent = 0;

    private final CollapsingWorkerThread signaler = new CollapsingWorkerThread("RConf-Signaler") {

        @Override
        protected void doWork() throws Throwable {
            int field;
            byte[] payload;
            synchronized (signalLock) {
                field = signalField;
                payload = signalPayload;
                signalPayload = null;
            }
            if (payload == null) {
                return;
            }
            device.signalRConf(field, payload);
            updater.trigger();
        }
    };

    private final CollapsingWorkerThread updater = new CollapsingWorkerThread("RConf-Updater") {

        @Override
        protected void doWork() throws Throwable {
            RConf.Entry[] out = device.queryRConf();
            if (out == null) {
                Logger.warning("Could not refresh RConf pane.");
            } else {
                entries = out;
            }
        }
    };

    private String path;

    /**
     * Create a new RConfComponent at the given location with a specified
     * device.
     * 
     * @param cx the X position.
     * @param cy the Y position.
     * @param path the path to this component
     * @param device the device to interact with.
     */
    public RConfComponent(int cx, int cy, String path, RConfable device) {
        super(cx, cy);
        this.path = path;
        this.device = device;
        updater.trigger();
        halfWidth = 100;
        halfHeight = 20;
    }

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        Rendering.drawBody(bodyColor, g, this);
        halfHeight = 10 * (entries.length + 1) + 5;
        g.setColor(Color.BLACK);
        int curY = centerY - halfHeight + 5;
        {
            boolean asTitle = true;
            for (RConf.Entry e : entries) {
                if (e.type == RConf.F_TITLE) {
                    asTitle = false;
                    break;
                }
            }
            g.setFont(asTitle ? Rendering.midlabels : Rendering.console);
            String str = updater.isDoingWork() ? "loading..." : this.path;
            int hw = g.getFontMetrics().stringWidth(str) / 2;
            if (hw + 10 > halfWidth) {
                halfWidth = hw + 10;
            }
            g.drawString(str, centerX - hw, curY + (asTitle ? 20 : 15));
            curY += 20;
        }
        int field = 0;
        for (RConf.Entry e : entries) {
            String str;
            int textShift = 15;
            g.setFont(Rendering.console);
            switch (e.type) {
            case RConf.F_TITLE:
                g.setFont(Rendering.midlabels);
                textShift = 15;
                String title = RConf.parseTextual(e.contents);
                str = title == null ? "<invalid:bad-title>" : title;
                break;
            case RConf.F_BOOLEAN:
                Boolean b = RConf.parseBoolean(e.contents);
                str = b == null ? "<invalid:bad-bool>" : "FALSE <- [" + b.toString() + "] -> TRUE ";
                break;
            case RConf.F_INTEGER:
                Integer i = RConf.parseInteger(e.contents);
                str = i == null ? "<invalid:bad-int>" : i.toString();
                break;
            case RConf.F_FLOAT:
                Float f = RConf.parseFloat(e.contents);
                str = f == null ? "<invalid:bad-float>" : f.toString();
                break;
            case RConf.F_STRING:
                String text = RConf.parseTextual(e.contents);
                str = text == null ? "<invalid:bad-str>" : text;
                break;
            case RConf.F_BUTTON:
                String label = RConf.parseTextual(e.contents);
                g.setColor(signalField == field && System.currentTimeMillis() - lastSent < 500 ? Color.GREEN : Color.RED);
                int wlabel = g.getFontMetrics().stringWidth(label) + 20;
                g.fillRect(centerX - wlabel / 2, curY + 1, wlabel, 18);
                g.setColor(Color.BLACK);
                g.drawRect(centerX - wlabel / 2, curY + 1, wlabel, 18);
                str = label == null ? "<invalid:bad-label>" : label;
                break;
            case RConf.F_CLUCK_REF:
                String ref = RConf.parseTextual(e.contents);
                str = ref == null ? "<invalid:bad-cluck>" : "@" + ref;
                break;
            default:
                str = "<invalid:bad-type>";
                break;
            }
            int hw = g.getFontMetrics().stringWidth(str) / 2;
            if (hw + 20 > halfWidth) {
                halfWidth = hw + 20;
            }
            g.drawString(str, centerX - hw, curY + textShift);
            curY += 20;
            field++;
        }
    }

    @Override
    public boolean onInteract(int x, int y) {
        int relY = y - centerY + halfHeight - 5;
        if (relY < 20) {
            updater.trigger();
        } else {
            int field = 0;
            for (RConf.Entry e : entries) {
                relY -= 20;
                if (relY < 20) {
                    byte[] payload = new byte[0];
                    switch (e.type) {
                    case RConf.F_BOOLEAN:
                        payload = new byte[] { (byte) (x < centerX ? 0 : 1) };
                        break;
                    case RConf.F_INTEGER:
                        Integer oldInt = RConf.parseInteger(e.contents);
                        String asked = JOptionPane.showInputDialog("Enter integer", oldInt == null ? "0" : Integer.toString(oldInt));
                        int newInt;
                        if (asked == null) {
                            break;
                        }
                        try {
                            newInt = Integer.parseInt(asked);
                        } catch (NumberFormatException ex) {
                            break;
                        }
                        payload = new byte[] { (byte) (newInt >> 24), (byte) (newInt >> 16), (byte) (newInt >> 8), (byte) newInt };
                        break;
                    case RConf.F_FLOAT:
                        Float oldFloat = RConf.parseFloat(e.contents);
                        asked = JOptionPane.showInputDialog("Enter float", oldFloat == null ? "0" : Float.toString(oldFloat));
                        float newFloat;
                        if (asked == null) {
                            break;
                        }
                        try {
                            newFloat = Float.parseFloat(asked);
                        } catch (NumberFormatException ex) {
                            break;
                        }
                        int intBits = Float.floatToIntBits(newFloat);
                        payload = new byte[] { (byte) (intBits >> 24), (byte) (intBits >> 16), (byte) (intBits >> 8), (byte) intBits };
                        break;
                    case RConf.F_STRING:
                        String oldString = RConf.parseTextual(e.contents);
                        asked = JOptionPane.showInputDialog("Enter string", oldString == null ? "" : oldString);
                        if (asked != null) {
                            payload = asked.getBytes();
                        }
                        break;
                    case RConf.F_CLUCK_REF:
                        Logger.info("TODO: Drag out the selected cluck component.");
                        break;
                    }
                    synchronized (signalLock) {
                        lastSent = System.currentTimeMillis();
                        signalField = field;
                        signalPayload = payload;
                        signaler.trigger();
                    }
                    break;
                }
                field++;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "RConf Access";
    }
}
