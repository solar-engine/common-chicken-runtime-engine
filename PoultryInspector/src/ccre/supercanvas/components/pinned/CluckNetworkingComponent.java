/*
 * Copyright 2014-2016 Colby Skeggs.
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
package ccre.supercanvas.components.pinned;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.Collection;

import ccre.cluck.Cluck;
import ccre.cluck.tcp.CluckTCPClient;
import ccre.cluck.tcp.TracingCluckTCPClient;
import ccre.net.Network;
import ccre.net.TrafficCounting;
import ccre.supercanvas.Rendering;
import ccre.supercanvas.SuperCanvasComponent;
import ccre.supercanvas.SuperCanvasPanel;

/**
 * A component that displays the current results from the
 * CountingNetworkProvider and allows control of the remote IP address.
 *
 * @author skeggsc
 */
public class CluckNetworkingComponent extends SuperCanvasComponent {

    private static final long serialVersionUID = 8969267415884377303L;

    /**
     * The remote address that represents that no connection is wanted.
     */
    public static final String DO_NOT_CONNECT = ":";
    private static final String[] optionNames = new String[] { "roboRIO (default)", "Local (default)", "roboRIO (2015)", "roboRIO (USB)", "roboRIO (non-FMS)", "roboRIO (alternate 1)", "roboRIO (alternate 2)", "Local (alternate 1)", "Local (alternate 2)", "Don't Connect" };
    private static final String[] optionAddrs = new String[] { "roboRIO-$T$E$A$M-FRC.local:5800", "127.0.0.1:1540", "roboRIO-$T$E$A$M.local:5800", "172.22.11.2:1540", "roboRIO-$T$E$A$M-FRC.local:1540", "roboRIO-$T$E$A$M-FRC.local:5805", "roboRIO-$T$E$A$M-FRC.local:1735", "127.0.0.1:80", "127.0.0.1:443", DO_NOT_CONNECT };

    private final StringBuilder address = new StringBuilder(optionAddrs[0]);
    private boolean expanded = false;
    private transient CluckTCPClient client;

    /**
     * Create a new CluckNetworkingComponent.
     */
    public CluckNetworkingComponent() {
    }

    /**
     * Create a new CluckNetworkingComponent with a specified remote address.
     *
     * @param address the default address.
     */
    public CluckNetworkingComponent(String address) {
        this.address.setLength(0);
        this.address.append(address);
    }

    private int firstMenuEntry = 0, menuEntryDelta = 1;

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        if (expanded) {
            String raddr = address.toString();
            if (System.currentTimeMillis() % 1000 < 500 && getPanel().editing == address) {
                raddr += "|";
            }
            String[] lines = new String[3 + optionNames.length];
            // Three header lines:
            lines[0] = "Type or select from list";
            lines[1] = raddr;
            lines[2] = getStatusMessage();

            System.arraycopy(optionNames, 0, lines, 3, optionNames.length);
            menuEntryDelta = fontMetrics.getHeight();
            int height = menuEntryDelta * lines.length + 10;
            Rendering.drawBody(Color.GRAY, g, screenWidth - 100, height / 2, 200, height);
            for (int i = 0; i < lines.length; i++) {
                if (i >= 3 && mouseX >= screenWidth - 200 && mouseY >= menuEntryDelta * i && mouseY < menuEntryDelta * (i + 1)) {
                    g.setColor(Color.CYAN);
                } else if (i >= 3 && optionAddrs[i - 3].equals(address.toString())) {
                    g.setColor(Color.GREEN);
                } else {
                    g.setColor(Color.WHITE);
                }
                g.drawString(lines[i], screenWidth - 195, fontMetrics.getAscent() + fontMetrics.getHeight() * i);
            }
            firstMenuEntry = menuEntryDelta * 3; // after the three header lines
        } else {
            if (getPanel().editmode) {
                g.setColor(contains(mouseX, mouseY) ? Color.CYAN : Color.WHITE);
            } else {
                g.setColor(contains(mouseX, mouseY) ? Color.GREEN : Color.BLACK);
            }
            StringBuilder sb = new StringBuilder(getStatusMessage());
            String summ = client == null ? null : client.getErrorSummary();
            if (summ != null) {
                sb.append(" (").append(summ).append(')');
            }
            sb.append(" ~").append(TrafficCounting.getRateBytesPerSecond() / 128).append("kbs/s");
            String countReport = sb.toString();
            g.drawString(countReport, screenWidth - fontMetrics.stringWidth(countReport), fontMetrics.getAscent());
        }
    }

    private synchronized String getStatusMessage() {
        if (client == null) {
            return "not ready";
        } else if (client.isReconnecting()) {
            if (client.isEstablished()) {
                return "establishing...";
            } else {
                return "connecting to " + client.getRemote() + "...";
            }
        } else if (client.isEstablished()) {
            return "active";
        } else {
            float pause_remain = (int) ((client.getReconnectDeadline() - System.currentTimeMillis()) / 100f) / 10f;
            if (pause_remain <= 0) {
                return "about to reconnect";
            } else {
                return "pausing for " + pause_remain + "s";
            }
        }
    }

    @Override
    protected void onChangePanel(SuperCanvasPanel panel) {
        updateConnection();
    }

    /**
     * Should the Cluck connection log its traffic data?
     */
    public static boolean useLoggingConnection = false;

    private synchronized void updateConnection() {
        String remote = calculateRemote();
        if (getPanel() == null || remote == null) {
            if (client != null) {
                client.terminate();
                client = null;
            }
        } else if (client == null) {
            client = useLoggingConnection ? new TracingCluckTCPClient(remote, Cluck.getNode(), "robot", null) : new CluckTCPClient(remote, Cluck.getNode(), "robot", null);
            client.setReconnectDelay(1000);
            client.setLogDuringNormalOperation(false);
            client.start();
        } else {
            if (!client.getRemote().equals(remote)) {
                client.setRemote(remote);
            }
        }
    }

    @Override
    public void onPressedEnter() {
        updateConnection();
    }

    private String calculateRemote() {
        if (address.toString().equals(DO_NOT_CONNECT)) { // don't connect
            return null;
        }
        char T = '?', E = '?', A = '?', M = '?';
        Collection<String> addresses = Network.listIPv4Addresses();
        for (String addr : addresses) {
            String[] spt = addr.split("[.]");
            if (spt.length == 4 && spt[0].equals("10")) {
                try {
                    int prefix = Integer.parseInt(spt[1]);
                    int suffix = Integer.parseInt(spt[2]);
                    if (prefix >= 0 && prefix < 100 && suffix > 0 && suffix < 100) {
                        T = (char) ((prefix / 10) + '0');
                        E = (char) ((prefix % 10) + '0');
                        A = (char) ((suffix / 10) + '0');
                        M = (char) ((suffix % 10) + '0');
                        break;
                    }
                } catch (NumberFormatException e) {
                    // Do nothing.
                }
            }
        }
        if (address.toString().contains("$T") && T == '?') {
            return null;
        }
        return address.toString().replace("$T", Character.toString(T)).replace("$E", Character.toString(E)).replace("$A", Character.toString(A)).replace("$M", Character.toString(M));
    }

    @Override
    public boolean contains(int x, int y) {
        if (expanded) {
            return x >= getPanel().getWidth() - 200 && y <= (firstMenuEntry + menuEntryDelta * optionAddrs.length);
        } else {
            return x >= getPanel().getWidth() - 100 && y <= 18;
        }
    }

    @Override
    public boolean onInteract(int x, int y) {
        return onSelect(x, y);
    }

    @Override
    public boolean onSelect(int x, int y) {
        if (y >= firstMenuEntry && expanded) {
            int menuId = (y - firstMenuEntry) / menuEntryDelta;
            if (menuId >= 0 && menuId < optionAddrs.length) {
                address.setLength(0);
                address.append(optionAddrs[menuId]);
                updateConnection();
                return true;
            }
        }
        expanded = !expanded;
        if (expanded) {
            getPanel().editing = address;
        } else if (getPanel().editing == address) {
            getPanel().editing = null;
        }
        getPanel().raise(this);
        return true;
    }
}
