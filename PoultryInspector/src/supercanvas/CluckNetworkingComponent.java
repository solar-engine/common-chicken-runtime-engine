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
package supercanvas;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import ccre.channel.EventOutput;
import ccre.cluck.Cluck;
import ccre.cluck.tcp.CluckTCPClient;
import ccre.ctrl.Ticker;
import ccre.net.CountingNetworkProvider;
import ccre.net.Network;
import ccre.util.CCollection;

/**
 * A component that displays the current results from the
 * CountingNetworkProvider.
 *
 * @author skeggsc
 */
public class CluckNetworkingComponent extends SuperCanvasComponent {

    private static final long serialVersionUID = 8969267415884377303L;

    private final StringBuilder address = new StringBuilder("roboRIO-$T$E$A$M.local:1540");
    private boolean expanded = false;
    private transient CluckTCPClient client;

    public CluckNetworkingComponent() {
        new Ticker(5000).send(new EventOutput() {
            @Override
            public void event() {
                updateConnection();
            }
        });
    }

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        if (expanded) {
            Rendering.drawBody(Color.GRAY, g, screenWidth - 100, 50, 200, 100);
            g.setColor(Color.WHITE);
            g.drawString(address.toString(), screenWidth - 190, 50);
        } else {
            if (getPanel().editmode) {
                g.setColor(contains(mouseX, mouseY) ? Color.CYAN : Color.WHITE);
            } else {
                g.setColor(contains(mouseX, mouseY) ? Color.GREEN : Color.BLACK);
            }
            String countReport = "~" + CountingNetworkProvider.getTotal() / 128 + "kbs";
            if (client == null) {
                countReport = "(not ready) " + countReport;
            } else if (client.isReconnecting()) {
                if (client.isEstablished()) {
                    countReport = "(establishing...) " + countReport;
                } else {
                    countReport = "(connecting to " + client.getRemote() + "...) " + countReport;
                }
            } else if (client.isEstablished()) {
                countReport = "(active) " + countReport;
            } else {
                float pause_remain = (int) ((client.getReconnectDeadline() - System.currentTimeMillis()) / 100f) / 10f;
                if (pause_remain <= 0) {
                    countReport = "(about to reconnect) " + countReport;
                } else {
                    countReport = "(pausing for " + pause_remain + "s) " + countReport;
                }
            }
            g.drawString(countReport, screenWidth - fontMetrics.stringWidth(countReport), fontMetrics.getAscent());
        }
    }

    @Override
    protected void onChangePanel(SuperCanvasPanel panel) {
        updateConnection();
    }

    private synchronized void updateConnection() {
        String remote = calculateRemote();
        if (getPanel() == null || remote == null) {
            if (client != null) {
                client.terminate();
                client = null;
            }
        } else if (client == null) {
            client = new CluckTCPClient(remote, Cluck.getNode(), "robot", "phidget");
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
        char T = '?', E = '?', A = '?', M = '?';
        CCollection<String> addresses = Network.listIPv4Addresses();
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
            return x >= getPanel().getWidth() - 200 && y <= 100;
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
