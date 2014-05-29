/*
 * Copyright 2013-2014 Colby Skeggs, Gregor Peach (Added Folders)
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

import ccre.channel.EventOutput;
import ccre.cluck.Cluck;
import ccre.cluck.CluckRemoteListener;
import ccre.cluck.rpc.RemoteProcedure;
import ccre.concurrency.CollapsingWorkerThread;
import ccre.ctrl.ExpirationTimer;
import ccre.ctrl.Ticker;
import ccre.log.Logger;
import ccre.net.CountingNetworkProvider;
import ccre.util.UniqueIds;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * The upgraded display panel for browsing CCRE networks.
 *
 * @author skeggsc
 */
@SuppressWarnings({"serial", "rawtypes"})
public final class IntelligenceMain extends JPanel implements CluckRemoteListener, MouseMotionListener, MouseWheelListener, MouseListener {

    /**
     * The width of the object pane.
     */
    public static final int paneWidth = 256;
    /**
     * The currently highlighted row in the object pane.
     */
    private int activeRow = -1;
    /**
     * The cached height of each row.
     */
    private int rowHeight = -1;
    /**
     * The mapping of the Remote addresses to the Remotes.
     */
    private final HashMap<String, Remote> remotes = new HashMap<String, Remote>(100);
    /**
     * A cached sorted version of the remotes.
     */
    private Remote[] sortRemotes = null;
    /**
     * The current mapping of remote names to entities.
     */
    private final LinkedHashMap<String, Entity> ents = new LinkedHashMap<String, Entity>(100);
    /**
     * The currently held entity.
     */
    private Entity activeEntity = null;
    /**
     * The relative position between the cursor and the entity.
     */
    private int relActiveX, relActiveY;
    /**
     * The mouse button pressed while dragging an entity.
     */
    private int mouseBtn;
    /**
     * An expiration timer to repaint the pane when appropriate.
     */
    private ExpirationTimer painter;
    /**
     * The number of bytes transmitted as of the last byte counting operation.
     */
    private long baseByteCount = 0;
    /**
     * The number of bytes transmitted during the last measurement period.
     */
    private long lastByteCount = 0;
    /**
     * The current scrolling position of the object pane.
     */
    private int currentPaneScroll = 0;
    /**
     * Array of folders.
     */
    private final Folder[] folders;
    /**
     * The active dialog, if any.
     */
    private PoultryDialog dialog;
    /**
     * The list of tabs.
     */
    private final java.util.List<Tab> tabs;
    private CollapsingWorkerThread researcher;
    private CollapsingWorkerThread discover;

    /**
     * Create and set up a new IntelligenceMain instance.
     */
    public IntelligenceMain() {
        folders = Folder.setupFolders();
        tabs = Tab.getTabs();
    }

    /**
     * Start the IntelligenceMain instance so that it runs.
     */
    public void start() {
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        this.addMouseWheelListener(this);
        this.discover = new CollapsingWorkerThread("Cluck-Discoverer") {
            @Override
            protected void doWork() {
                IPProvider.connect();
            }
        };
        new Ticker(1000).send(new EventOutput() {
            @Override
            public void event() {
                long cur = CountingNetworkProvider.getTotal();
                lastByteCount = cur - baseByteCount;
                baseByteCount = cur;
            }
        });
        Cluck.getNode().getRPCManager().publish("display-dialog", new RemoteProcedure() {
            @Override
            public void invoke(byte[] in, OutputStream out) {
                if (dialog != null) {
                    try {
                        out.close();
                    } catch (IOException ex) {
                        Logger.warning("IOException from return from procedure!", ex);
                    }
                    return;
                }
                dialog = new PoultryDialog(new String(in), out);
            }
        });
        painter = new ExpirationTimer();
        painter.schedule(50, new EventOutput() {
            @Override
            public void event() {
                repaint();
            }
        });
        final String searchLinkName = UniqueIds.global.nextHexId("big-brother");
        this.researcher = new CollapsingWorkerThread("Cluck-Researcher") {
            @Override
            protected void doWork() throws Throwable {
                remotes.clear();
                sortRemotes = null;
                Cluck.getNode().cycleSearchRemotes(searchLinkName);
            }
        };
        Cluck.getNode().startSearchRemotes(searchLinkName, this);
        painter.start();
        triggerResearch();
    }

    /**
     * Search for new remotes at the next available opportunity.
     */
    public void triggerResearch() {
        researcher.trigger();
    }

    /**
     * Rediscover the remote address at the next available opportunity.
     */
    public void triggerDiscover() {
        discover.trigger();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (mouseBtn == MouseEvent.BUTTON3) {
            for (Entity ent : ents.values()) {
                if (ent.isInCanvas() && ent.isOver(e.getPoint())) {
                    ent.interact(e.getX(), e.getY());
                    return;
                }
            }
            return;
        }
        if (activeEntity != null) {
            boolean a = activeEntity.isInCanvas();
            activeEntity.moveTo(relActiveX + e.getX(), relActiveY + e.getY());
            if (a != (activeEntity.isInCanvas())) {
                sortRemotes = null;
            }
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int curX = e.getX();
        int curY = e.getY() - currentPaneScroll;
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
    public void mouseWheelMoved(MouseWheelEvent e) {
        currentPaneScroll -= e.getWheelRotation() * 2;
        if (currentPaneScroll > 0) {
            currentPaneScroll = 0;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int index = 0;
        for (Iterator<Tab> it = tabs.iterator(); it.hasNext();) {
            Tab t = it.next();
            if (this.getWidth() - 80 < e.getX() && 50 + index * 35 < e.getY() && this.getWidth() > e.getX() && (50 + index * 35) + 30 > e.getY()) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    if (JOptionPane.showConfirmDialog(this, "Really delete?") == JOptionPane.OK_OPTION) {
                        System.out.println("removing");
                        it.remove();
                        Tab.removeTab(t);
                    }
                } else {
                    t.enforceTab(ents, remotes);
                }
                return;
            }
            index++;
        }
        if (this.getWidth() - 30 < e.getX() && 50 + index * 35 < e.getY() && this.getWidth() > e.getX() && (50 + index * 35) + 30 > e.getY()) {
            try {
                String result = JOptionPane.showInputDialog("What Name?");
                if (result == null) {
                    return;
                }
                if (result.isEmpty()) {
                    return;
                }
                Entity[] ent = new Entity[ents.values().size()];
                ents.values().toArray(ent);
                Tab t = new Tab(result, ent);
                tabs.add(t);
                Tab.addTab(t);
            } catch (IOException ex) {
                Logger.warning("Could not set up tab list!", ex);
            }
        }
        if (dialog != null && dialog.isOver(paneWidth, getWidth(), getHeight(), e.getX(), e.getY())) {
            if (dialog.press(paneWidth, getWidth(), getHeight(), e.getX(), e.getY())) {
                dialog = null;
            }
            return;
        }
        mouseBtn = e.getButton();
        if (e.getButton() == MouseEvent.BUTTON3) {
            for (Entity ent : ents.values()) {
                if (ent.isInCanvas() && ent.isOver(e.getPoint())) {
                    ent.interact(e.getX(), e.getY());
                    return;
                }
            }
            return;
        }
        if (paneWidth <= e.getX() && e.getX() < paneWidth + 16) {
            if (32 <= e.getY() && e.getY() < 48) {
                currentPaneScroll += 10;
            } else if (getHeight() - 48 <= e.getY() && e.getY() < getHeight() - 32) {
                currentPaneScroll -= 10;
            }
            if (currentPaneScroll > 0) {
                currentPaneScroll = 0;
            }
        }
        for (Entity ent : ents.values()) {
            if (ent.isInCanvas() && ent.isOver(e.getPoint())) {
                activeEntity = ent;
                relActiveX = ent.getCenterX() - e.getX();
                relActiveY = ent.getCenterY() - e.getY();
                return;
            }
        }
        Remote[] rms = sortRemotes;
        if (e.getX() < paneWidth && rms != null) {
            int row = (e.getY() - currentPaneScroll) / rowHeight;
            if (row >= 0 && row < rms.length) {
                Remote rem = rms[row];
                if (rem instanceof Folder) {
                    ((Folder) rem).open = !((Folder) rem).open;
                    sortRemotes = null;
                    return;
                }
                if (ents.containsKey(rem.path)) {
                    activeEntity = ents.get(rem.path);
                    relActiveX = relActiveY = 0;
                    sortRemotes = null;
                    return;
                } else {
                    Entity ent = new Entity(rem, e.getX(), e.getY());
                    ents.put(rem.path, ent);
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
            remotes.put(remote, new Remote(remote, remoteType));
            sortRemotes = null;
        } else if (old.type != remoteType) {
            Logger.warning("Remote type modified for " + remote + "!");
            remotes.put(remote, new Remote(remote, remoteType));
            sortRemotes = null;
        }
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        g.setFont(Rendering.console);
        g.setColor(Color.RED);
        g.fillRect(0, 0, paneWidth, h);
        g.setColor(Color.WHITE);
        g.fillRect(paneWidth, 0, w - paneWidth, h);
        g.setColor(Color.YELLOW);
        g.fillRect(paneWidth, 32, 16, 16);
        g.fillRect(paneWidth, h - 48, 16, 16);
        g.setColor(Color.RED);
        g.drawLine(paneWidth, 47, paneWidth + 7, 32); // ABOUT TO MAKE THESE SCROLL
        g.drawLine(paneWidth + 15, 47, paneWidth + 8, 32);
        g.drawLine(paneWidth, h - 48, paneWidth + 7, h - 33);
        g.drawLine(paneWidth + 15, h - 48, paneWidth + 8, h - 33);
        FontMetrics fontMetrics = g.getFontMetrics();
        int lh = fontMetrics.getHeight();
        g.setColor(Color.RED);
        g.drawString("Left-click to move", paneWidth, fontMetrics.getAscent());
        g.drawString("Right-click to interact", paneWidth, fontMetrics.getAscent() + lh);
        String countReport = "Estimated Traffic: " + CountingNetworkProvider.getTotal() + "B (" + (CountingNetworkProvider.getTotal() / 128) + "kbits)";
        g.drawString(countReport, w - fontMetrics.stringWidth(countReport), fontMetrics.getAscent());
        countReport = "Usage: " + (lastByteCount / 128) + "kbits/sec";
        g.drawString(countReport, w - fontMetrics.stringWidth(countReport), fontMetrics.getAscent() + lh);
        Remote[] sremotes = sortRemotes;
        if (sortRemotes == null) {
            ArrayList<Remote> loc;
            try {
                loc = new ArrayList<Remote>(remotes.values());
            } catch (ConcurrentModificationException c) {
                // Wait until next cycle.
                return;
            }
            for (Remote r : loc) {
                String p = r.path;
                if (p.endsWith(".output")) {
                    r.paired = remotes.get(p.substring(0, p.length() - ".output".length()) + ".input");
                } else if (p.endsWith(".input")) {
                    r.paired = remotes.get(p.substring(0, p.length() - ".input".length()) + ".output");
                } else {
                    r.paired = null;
                }
            }
            for (Map.Entry<String, Entity> key : ents.entrySet()) {
                if (key.getValue().isInCanvas()) {
                    loc.remove(remotes.get(key.getKey()));
                }
            }
            Collections.sort(loc);
            loc.addAll(Arrays.asList(folders));
            for (Folder f : folders) {
                f.contents.clear();
                f.hascontents = false;
            }
            for (Iterator<Remote> it = loc.iterator(); it.hasNext();) {
                Remote r = it.next();
                r.inFolder = false;
                for (Folder f : folders) {
                    if (f.isInside(r)) {
                        it.remove();
                        f.hascontents = true;
                        if (f.open) {
                            f.contents.add(r);
                        }
                    }
                }
            }
            for (Folder f : folders) {
                f.place = loc.indexOf(f);
                for (Remote r : f.contents) {
                    r.inFolder = true;
                    loc.add(f.place + 1, r);
                }
            }
            sremotes = loc.toArray(new Remote[loc.size()]);
            sortRemotes = sremotes;
        }
        g.setColor(Color.YELLOW);
        Graphics subscreen = g.create(1, 1, paneWidth - 2, h - 2);
        subscreen.fillRect(0, 0, paneWidth, h);
        if (activeRow != -1) {
            subscreen.setColor(Color.ORANGE);
            subscreen.fillRect(0, activeRow * rowHeight + currentPaneScroll, paneWidth, rowHeight);
        }
        subscreen.setColor(Color.BLACK);
        rowHeight = lh;
        int suby = fontMetrics.getAscent() + currentPaneScroll;
        for (Remote rem : sremotes) {
            subscreen.drawString(rem.toString(), 3, suby);
            suby += lh;
        }
        for (Entity ent : ents.values()) {
            if (ent.isInCanvas()) {
                ent.render(g);
            }
        }
        if (dialog != null) {
            if (dialog.render(g, paneWidth, w, h)) {
                dialog = null;
            }
        }
        int index = -1;
        for (Tab t : tabs) {
            index++;
            g.setColor(Color.CYAN);
            g.fillRect(this.getWidth() - 80, 50 + index * 35, 80, 30);
            g.setColor(Color.BLACK);
            g.drawRect(this.getWidth() - 81, 49 + index * 35, 82, 31);
            g.drawString(t.name, this.getWidth() - 80 + 7, 50 + index * 35 + 19);
        }
        index++;
        g.setColor(Color.CYAN);
        g.fillRect(this.getWidth() - 30, 50 + index * 35, 30, 30);
        g.setColor(Color.BLACK);
        g.drawRect(this.getWidth() - 31, 49 + index * 35, 82, 31);
        g.setColor(Color.BLUE);
        g.drawString("+", this.getWidth() - 30 + 10, 60 + index * 35);
        if (painter != null) {
            painter.feed();
        }
    }

}
