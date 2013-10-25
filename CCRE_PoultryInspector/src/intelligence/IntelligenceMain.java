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

import ccre.chan.FloatInputProducer;
import ccre.chan.FloatOutput;
import ccre.chan.FloatStatus;
import ccre.cluck.CluckGlobals;
import ccre.cluck.CluckNode;
import ccre.cluck.CluckRemoteListener;
import ccre.concurrency.ReporterThread;
import ccre.event.Event;
import ccre.event.EventConsumer;
import ccre.event.EventLogger;
import ccre.event.EventSource;
import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.log.LoggingTarget;
import ccre.log.MultiTargetLogger;
import ccre.log.NetworkAutologger;
import ccre.util.ExpirationTimer;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

public class IntelligenceMain extends JPanel implements CluckRemoteListener, MouseMotionListener, MouseListener {
    
    public static final Color canvasBackground = Color.WHITE;
    public static final Color paneBackground = Color.YELLOW;
    public static final Color highlight = Color.ORANGE;
    public static final Color active = Color.RED;
    public static final Color foreground = Color.BLACK;
    public static final Font console = new Font("Monospaced", Font.PLAIN, 11);
    public static final int paneWidth = 256;
    public final CluckNode node;
    protected int activeRow = -1;
    protected int rowHeight = -1;
    protected final String lrecv;
    protected final HashMap<String, Remote> remotes = new HashMap<String, Remote>();
    protected Remote[] sortRemotes = null;
    protected final LinkedHashMap<String, Entity> ents = new LinkedHashMap<String, Entity>();
    protected Entity activeEntity = null;
    protected int relActiveX, relActiveY;
    protected final ExpirationTimer painter = new ExpirationTimer();
    
    private IntelligenceMain(String[] args, CluckNode node) {
        this.node = node;
        lrecv = "big-brother-" + Integer.toHexString(args.hashCode());
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        this.node.startSearchRemotes(lrecv, this);
        painter.schedule(50, new EventConsumer() {
            @Override
            public void eventFired() {
                repaint();
            }
        });
        painter.start();
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        if (activeEntity != null) {
            boolean a = activeEntity.centerX < paneWidth;
            activeEntity.centerX = relActiveX + e.getX();
            activeEntity.centerY = relActiveY + e.getY();
            if (a != (activeEntity.centerX < paneWidth)) {
                sortRemotes = null;
            }
            repaint();
        }
    }
    
    @Override
    public void mouseMoved(MouseEvent e) {
        int curX = e.getX();
        int curY = e.getY();
        int row = -1;
        if (curX < paneWidth && rowHeight != -1) {
            row = curY / rowHeight;
        }
        if (activeRow != row) {
            activeRow = row;
            repaint();
        }
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            for (Entity ent : ents.values()) {
                if (ent.centerX >= paneWidth && ent.isOver(e.getPoint())) {
                    ent.interact(e.getX() - ent.centerX, e.getY() - ent.centerY);
                    return;
                }
            }
            return;
        }
        for (Entity ent : ents.values()) {
            if (ent.centerX >= paneWidth && ent.isOver(e.getPoint())) {
                activeEntity = ent;
                relActiveX = ent.centerX - e.getX();
                relActiveY = ent.centerY - e.getY();
                return;
            }
        }
        Remote[] rms = sortRemotes;
        if (e.getX() < paneWidth && rms != null) {
            int row = e.getY() / rowHeight;
            if (row >= 0 && row < rms.length) {
                Remote rem = rms[row];
                if (ents.containsKey(rem.remote)) {
                    activeEntity = ents.get(rem.remote);
                    relActiveX = relActiveY = 0;
                    sortRemotes = null;
                    return;
                } else {
                    Entity ent = new Entity(rem, e.getX(), e.getY());
                    ents.put(rem.remote, ent);
                    activeEntity = ent;
                    relActiveX = relActiveY = 0;
                    sortRemotes = null;
                    return;
                }
            }
        }
        activeEntity = null;
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        activeEntity = null;
    }
    
    @Override
    public void mouseEntered(MouseEvent e) {
    }
    
    @Override
    public void mouseExited(MouseEvent e) {
    }
    
    @Override
    public void handle(String remote, int remoteType) {
        Remote old = remotes.get(remote);
        if (old == null) {
            remotes.put(remote, new Remote(remote, remoteType, node));
            sortRemotes = null;
        } else if (old.type != remoteType) {
            Logger.warning("Remote type modified for " + remote + "!");
            remotes.put(remote, new Remote(remote, remoteType, node));
            sortRemotes = null;
        }
        repaint();
    }
    
    public void research() {
        node.cycleSearchRemotes(lrecv);
        // TODO: Remove old entries
    }
    
    @Override
    public void paint(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        g.setColor(active);
        g.fillRect(0, 0, paneWidth, h);
        g.setColor(canvasBackground);
        g.fillRect(paneWidth, 0, w - paneWidth, h);
        g.setFont(console);
        FontMetrics fontMetrics = g.getFontMetrics();
        int lh = fontMetrics.getHeight();
        g.setColor(active);
        g.drawString("Left-click to move", paneWidth, fontMetrics.getAscent());
        g.drawString("Right-click to interact", paneWidth, fontMetrics.getAscent() + lh);
        Remote[] sremotes = sortRemotes;
        if (sortRemotes == null) {
            ArrayList<Remote> loc = new ArrayList<Remote>(remotes.values());
            for (Map.Entry<String, Entity> key : ents.entrySet()) {
                if (key.getValue().centerX >= paneWidth) {
                    loc.remove(remotes.get(key.getKey()));
                }
            }
            Collections.sort(loc);
            sremotes = loc.toArray(new Remote[loc.size()]);
            sortRemotes = sremotes;
        }
        g.setColor(paneBackground);
        Graphics subscreen = g.create(1, 1, paneWidth - 2, h - 2);
        subscreen.fillRect(0, 0, paneWidth, h);
        if (activeRow != -1) {
            subscreen.setColor(highlight);
            subscreen.fillRect(0, activeRow * rowHeight, paneWidth, rowHeight);
        }
        subscreen.setColor(foreground);
        rowHeight = lh;
        int suby = fontMetrics.getAscent();
        for (Remote rem : sremotes) {
            subscreen.drawString(rem.toString(), 3, suby);
            suby += lh;
        }
        for (Entity ent : ents.values()) {
            if (ent.centerX >= paneWidth) {
                ent.render(g);
            }
        }
        painter.feed();
    }
    
    public static void main(String[] args) {
        CluckGlobals.ensureInitializedCore();
        final FloatStatus time = new FloatStatus();
        final Event prod = new Event();
        new ReporterThread("clock") {
            private final long beginning = System.currentTimeMillis();
            @Override
            protected void threadBody() throws Throwable {
                long last = System.currentTimeMillis();
                while (true) {
                    long now = System.currentTimeMillis();
                    time.writeValue((now - beginning) / 4000.0f - 2);
                    Thread.sleep(50);
                    if (now > last + 700) {
                        if (now > last + 5000) {
                            last = now;
                        }
                        last += 700;
                        prod.produce();
                    }
                }
            }
        }.start();
        CluckGlobals.node.publish("time", (FloatInputProducer) time);
        CluckGlobals.node.publish("test", new EventLogger(LogLevel.INFO, "Test."));
        CluckGlobals.node.publish("ticker", (EventSource) prod);
        NetworkAutologger.register();
        JFrame frame = new JFrame("Intelligence Panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(640, 480);
        JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JScrollPane scroll = new JScrollPane();
        JList lstErrors = new JList();
        DefaultListModel dlm = new DefaultListModel();
        lstErrors.setModel(dlm);
        LoggingTarget lt = new ListModelLogger(dlm, lstErrors);
        Logger.target = new MultiTargetLogger(Logger.target, lt);
        scroll.setViewportView(lstErrors);
        jsp.setRightComponent(scroll);
        jsp.setLeftComponent(new IntelligenceMain(args, CluckGlobals.node));
        jsp.setDividerLocation(2 * 480 / 3);
        jsp.setResizeWeight(0.7);
        frame.add(jsp);
        frame.setVisible(true);
    }
}
