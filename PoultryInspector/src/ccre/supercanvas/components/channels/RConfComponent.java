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
import java.util.Objects;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.channel.EventOutput;
import ccre.concurrency.CollapsingWorkerThread;
import ccre.rconf.RConf;
import ccre.rconf.RConfable;
import ccre.supercanvas.DraggableBoxComponent;
import ccre.supercanvas.Rendering;
import ccre.supercanvas.SuperCanvasComponent;
import ccre.supercanvas.SuperCanvasPanel;
import ccre.supercanvas.components.palette.NetworkPaletteComponent;
import ccre.timers.Ticker;

/**
 * A SuperCanvas-based component to allow interaction with RConf data.
 *
 * @author skeggsc
 */
public class RConfComponent extends DraggableBoxComponent {
    private static Logger logger = LoggerFactory.getLogger(RConfComponent.class);

    private final class UpdatingWorker extends CollapsingWorkerThread {
        private UpdatingWorker() {
            super("RConf-Updater");
        }

        @Override
        protected void doWork() throws Throwable {
            RConf.Entry[] out = device.queryRConf();
            if (out == null) {
                lastSignalSucceeded = false;
                consecutiveUpdateFailures++;
                setAutoRefreshDelay(autoRefreshDelay); // refresh it because the additional failures may have made it slower.
            } else {
                lastSignalSucceeded = true;
                entries = out;
                consecutiveUpdateFailures = 0;
            }
            showSignalSuccessUntil = System.currentTimeMillis() + SIGNAL_SUCCESS_FLASH_TIME;
        }
    }

    private final class SignalingWorker extends CollapsingWorkerThread {
        private SignalingWorker() {
            super("RConf-Signaler");
        }

        private int signalField = -1;
        private byte[] signalPayload;
        private long lastSent = 0;

        @Override
        protected void doWork() throws Throwable {
            int field;
            byte[] payload;
            synchronized (this) {
                field = signalField;
                payload = signalPayload;
                signalPayload = null;
            }
            if (payload == null) {
                return;
            }
            lastSignalSucceeded = device.signalRConf(field, payload);
            showSignalSuccessUntil = System.currentTimeMillis() + SIGNAL_SUCCESS_FLASH_TIME;
            getUpdater().trigger();
        }

        public synchronized void signal(int field, byte[] payload) {
            lastSent = System.currentTimeMillis();
            signalField = field;
            signalPayload = payload;
            trigger();
        }

        public synchronized boolean recentlySent(int field, int within) {
            return signalField == field && System.currentTimeMillis() - lastSent < within;
        }
    }

    private static final long serialVersionUID = 6222627208004874042L;

    private static final Color bodyColor = Color.ORANGE;
    private static final Color successColor = Color.GREEN;
    private static final Color failureColor = Color.RED;
    private static final int SIGNAL_SUCCESS_FLASH_TIME = 500;

    private RConf.Entry[] entries = new RConf.Entry[0];
    protected final RConfable device;

    private boolean lastSignalSucceeded = false;
    private long showSignalSuccessUntil = 0;

    private transient int consecutiveUpdateFailures = 0;

    private transient SignalingWorker signaler;
    private transient UpdatingWorker updater;

    private String path;

    private transient Integer autoRefreshDelay = null;
    private transient Ticker autoRefreshTicker = null;

    @Override
    protected synchronized void onChangePanel(SuperCanvasPanel newPanel) {
        if (newPanel == null) {
            if (signaler != null) {
                signaler.terminate();
                signaler = null;
            }
            if (updater != null) {
                updater.terminate();
                updater = null;
            }
            setAutoRefreshDelay(null);
        }
    }

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
        getUpdater().trigger();
        halfWidth = 100;
        halfHeight = 20;
    }

    private synchronized void setAutoRefreshDelay(Integer delay) {
        if (delay != null && delay < 10000) {
            delay = (int) Math.min(Math.round(delay * Math.pow(2, consecutiveUpdateFailures / 5f)), 10000);
        }
        if (delay != null && delay < 10) {
            delay = 10;
        }
        if (Objects.equals(delay, autoRefreshDelay)) {
            return;
        }
        autoRefreshDelay = delay;
        if (autoRefreshTicker != null) {
            autoRefreshTicker.terminate();
            autoRefreshTicker = null;
        }
        if (delay != null) {
            autoRefreshTicker = new Ticker(delay);
            autoRefreshTicker.send(new EventOutput() {
                @Override
                public void event() {
                    if (getPanel() == null) {
                        setAutoRefreshDelay(null);
                    } else {
                        getUpdater().trigger();
                    }
                }
            });
        }
    }

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        Rendering.drawBody(Rendering.blend(bodyColor, lastSignalSucceeded ? successColor : failureColor, (showSignalSuccessUntil - System.currentTimeMillis()) / (float) SIGNAL_SUCCESS_FLASH_TIME), g, this);
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
            String str = getUpdater().isDoingWork() ? "loading..." : this.path;
            int hw = g.getFontMetrics().stringWidth(str) / 2;
            if (hw + 10 > halfWidth) {
                halfWidth = hw + 10;
            }
            g.drawString(str, centerX - hw, curY + (asTitle ? 20 : 15));
            curY += 20;
        }
        int field = 0;
        Integer newAutoRefreshDelay = null; // don't refresh by default
        for (RConf.Entry e : entries) {
            String str;
            int textShift = 15;
            g.setFont(Rendering.console);
            if (e.type == RConf.F_TITLE) {
                g.setFont(Rendering.midlabels);
                textShift = 15;
            }
            switch (e.type) {
            case RConf.F_TITLE:
                String title = e.parseTextual();
                str = title == null ? "<invalid:bad-title>" : title;
                break;
            case RConf.F_BOOLEAN:
                Boolean b = e.parseBoolean();
                str = b == null ? "<invalid:bad-bool>" : "FALSE <- [" + b.toString() + "] -> TRUE ";
                break;
            case RConf.F_BUTTON:
                String label = e.parseTextual();
                g.setColor(signaler != null && signaler.recentlySent(field, 500) ? Color.GREEN : Color.RED);
                int wlabel = g.getFontMetrics().stringWidth(label) + 20;
                g.fillRect(centerX - wlabel / 2, curY + 1, wlabel, 18);
                g.setColor(Color.BLACK);
                g.drawRect(centerX - wlabel / 2, curY + 1, wlabel, 18);
                str = label == null ? "<invalid:bad-label>" : label;
                break;
            case RConf.F_AUTO_REFRESH:
                field++;
                newAutoRefreshDelay = e.parseInteger();
                continue;
            default:
                str = e.toString();
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
        setAutoRefreshDelay(newAutoRefreshDelay);
        if (getPanel().editmode) {
            g.setColor(new Color(255, 0, 0, 128));
            g.fillOval(centerX + halfWidth - 10, centerY + halfHeight - 10, 8, 8);
        }
    }

    @Override
    public boolean onSelect(int x, int y) {
        return checkDelete(x, y) || super.onSelect(x, y);
    }

    private boolean checkDelete(int x, int y) {
        if (!getPanel().editmode || centerY + halfHeight - 10 > y || y > centerY + halfHeight - 2 || centerX + halfWidth - 10 > x || x > centerX + halfWidth - 2) {
            return false;
        }
        if (this.onDelete(false)) {
            getPanel().remove(this);
        } else {
            logger.warn("Component deletion disallowed: " + this);
        }
        return true;
    }

    @Override
    protected boolean containsForInteract(int x, int y) {
        int relY = y - centerY + halfHeight - 5;
        if (relY >= 20) {
            for (RConf.Entry e : entries) {
                relY -= 20;
                if (relY < 20) {
                    return e.type == RConf.F_BUTTON || e.type == RConf.F_CLUCK_REF;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onInteract(int x, int y) {
        if (checkDelete(x, y)) {
            return true;
        }
        int relY = y - centerY + halfHeight - 5;
        if (relY < 20) {
            getUpdater().trigger();
        } else {
            int field = 0;
            for (RConf.Entry e : entries) {
                if (e.type == RConf.F_AUTO_REFRESH) {
                    field++;
                    continue;
                }
                relY -= 20;
                if (relY < 20) {
                    byte[] payload = null;
                    switch (e.type) {
                    case RConf.F_BOOLEAN:
                        payload = new byte[] { (byte) (x < centerX ? 0 : 1) };
                        break;
                    case RConf.F_INTEGER:
                        Integer oldInt = e.parseInteger();
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
                        Float oldFloat = e.parseFloat();
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
                        String oldString = e.parseTextual();
                        asked = JOptionPane.showInputDialog("Enter string", oldString == null ? "" : oldString);
                        if (asked != null) {
                            payload = asked.getBytes();
                        }
                        break;
                    case RConf.F_CLUCK_REF:
                        String rawRef = e.parseTextual();
                        if (rawRef != null) {
                            NetworkPaletteComponent comp = getPanel().getAny(NetworkPaletteComponent.class);
                            if (comp == null) {
                                logger.warn("A network palette must be available in order to drag out anything from RConf components!");
                            } else {
                                String ref = path.contains("/") ? path.substring(0, path.lastIndexOf('/') + 1) + rawRef : rawRef;
                                SuperCanvasComponent nent = comp.getComponentFor(x, y, ref);
                                if (nent == null) {
                                    logger.warn("No network entry could be found for reference: " + ref);
                                } else {
                                    getPanel().add(nent);
                                    getPanel().startDrag(nent, x, y);
                                }
                            }
                        }
                        break;
                    case RConf.F_AUTO_REFRESH:
                        break;
                    default:
                        payload = new byte[0];
                    }
                    if (payload != null) {
                        if (signaler == null) {
                            signaler = new SignalingWorker();
                        }
                        signaler.signal(field, payload);
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

    private synchronized UpdatingWorker getUpdater() {
        if (updater == null) {
            updater = new UpdatingWorker();
        }
        return updater;
    }
}
