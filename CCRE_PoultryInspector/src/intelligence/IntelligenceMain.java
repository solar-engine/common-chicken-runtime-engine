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

import ccre.chan.FloatStatus;
import ccre.cluck.*;
import ccre.concurrency.CollapsingWorkerThread;
import ccre.event.EventConsumer;
import ccre.event.EventSource;
import ccre.log.*;
import ccre.ctrl.ExpirationTimer;
import ccre.ctrl.Ticker;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/**
 * The upgraded display panel for browsing CCRE networks.
 *
 * @author skeggsc
 */
public class IntelligenceMain extends JPanel implements CluckRemoteListener, MouseMotionListener, MouseWheelListener, MouseListener {

    /**
     * The background color for the main canvas.
     */
    public static final Color canvasBackground = Color.WHITE;
    /**
     * The background color for the object pane.
     */
    public static final Color paneBackground = Color.YELLOW;
    /**
     * The highlight color.
     */
    public static final Color highlight = Color.ORANGE;
    /**
     * The active selection color.
     */
    public static final Color active = Color.RED;
    /**
     * The foreground color.
     */
    public static final Color foreground = Color.BLACK;
    /**
     * The font used for everything.
     */
    public static final Font console = new Font("Monospaced", Font.PLAIN, 11);
    /**
     * The width of the object pane.
     */
    public static final int paneWidth = 256;
    /**
     * The CluckNode that this displays from.
     */
    protected final CluckNode node;
    /**
     * The currently highlighted row in the object pane.
     */
    protected int activeRow = -1;
    /**
     * The cached height of each row.
     */
    protected int rowHeight = -1;
    /**
     * The name of the searching link.
     */
    protected final String searchLinkName;
    /**
     * The mapping of the Remote addresses to the Remotes.
     */
    protected final HashMap<String, Remote> remotes = new HashMap<String, Remote>();
    /**
     * A cached sorted version of the remotes.
     */
    protected Remote[] sortRemotes = null;
    /**
     * The current mapping of remote names to entities.
     */
    protected final LinkedHashMap<String, Entity> ents = new LinkedHashMap<String, Entity>();
    /**
     * The currently held entity.
     */
    protected Entity activeEntity = null;
    /**
     * The relative position between the cursor and the entity.
     */
    protected int relActiveX, relActiveY;
    /**
     * The mouse button pressed while dragging an entity.
     */
    protected int mouseBtn;
    /**
     * An expiration timer to repaint the pane when appropriate.
     */
    protected final ExpirationTimer painter = new ExpirationTimer();
    /**
     * The number of bytes transmitted as of the last byte counting operation.
     */
    protected int baseByteCount = 0;
    /**
     * The number of bytes transmitted during the last measurement period.
     */
    protected int lastByteCount = 0;
    /**
     * The current scrolling position of the object pane.
     */
    protected int currentPaneScroll = 0;
    /**
     * Array of folders.
     */
    protected final Folder[] folders = new Folder[]{new Folder("Phidget", "^phidget")};

    /**
     * Create a new Intelligence Panel.
     *
     * @param args The main arguments to the program.
     * @param node The cluck node that this panel will display.
     * @param seconds An event that will be produced every second.
     *
     */
    private IntelligenceMain(String[] args, CluckNode node, EventSource seconds) {
        this.node = node;
        searchLinkName = "big-brother-" + Integer.toHexString(args.hashCode());
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        this.addMouseWheelListener(this);
        CollapsingWorkerThread discover = new CollapsingWorkerThread("Cluck-Discoverer") {
            @Override
            protected void doWork() {
                IPProvider.connect();
            }
        };
        CluckGlobals.node.publish("rediscover", discover);
        CollapsingWorkerThread researcher = new CollapsingWorkerThread("Cluck-Researcher") {
            @Override
            protected void doWork() throws Throwable {
                research();
            }
        };
        CluckGlobals.node.publish("search", researcher);
        seconds.addListener(new EventConsumer() {
            @Override
            public void eventFired() {
                int cur = IntelligenceMain.this.node.estimatedByteCount;
                lastByteCount = cur - baseByteCount;
                baseByteCount = cur;
            }
        });
        painter.schedule(50, new EventConsumer() {
            @Override
            public void eventFired() {
                repaint();
            }
        });
        painter.start();
        this.node.startSearchRemotes(searchLinkName, this);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (mouseBtn == MouseEvent.BUTTON3) {
            for (Entity ent : ents.values()) {
                if (ent.centerX >= paneWidth && ent.isOver(e.getPoint())) {
                    ent.interact(e.getX() - ent.centerX, e.getY() - ent.centerY);
                    return;
                }
            }
            return;
        }
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
        mouseBtn = e.getButton();
        if (e.getButton() == MouseEvent.BUTTON3) {
            for (Entity ent : ents.values()) {
                if (ent.centerX >= paneWidth && ent.isOver(e.getPoint())) {
                    ent.interact(e.getX() - ent.centerX, e.getY() - ent.centerY);
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
            if (ent.centerX >= paneWidth && ent.isOver(e.getPoint())) {
                activeEntity = ent;
                relActiveX = ent.centerX - e.getX();
                relActiveY = ent.centerY - e.getY();
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
            remotes.put(remote, new Remote(remote, remoteType, node));
            sortRemotes = null;
        } else if (old.type != remoteType) {
            Logger.warning("Remote type modified for " + remote + "!");
            remotes.put(remote, new Remote(remote, remoteType, node));
            sortRemotes = null;
        }
        repaint();
    }

    /**
     * Repeat searching for remote objects.
     */
    public void research() {
        node.cycleSearchRemotes(searchLinkName);
        // TODO: Remove old entries
    }

    @Override
    public void paint(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        g.setFont(console);
        g.setColor(active);
        g.fillRect(0, 0, paneWidth, h);
        g.setColor(canvasBackground);
        g.fillRect(paneWidth, 0, w - paneWidth, h);
        g.setColor(paneBackground);
        g.fillRect(paneWidth, 32, 16, 16);
        g.fillRect(paneWidth, h - 48, 16, 16);
        g.setColor(active);
        g.drawLine(paneWidth, 47, paneWidth + 7, 32); // ABOUT TO MAKE THESE SCROLL
        g.drawLine(paneWidth + 15, 47, paneWidth + 8, 32);
        g.drawLine(paneWidth, h - 48, paneWidth + 7, h - 33);
        g.drawLine(paneWidth + 15, h - 48, paneWidth + 8, h - 33);
        FontMetrics fontMetrics = g.getFontMetrics();
        int lh = fontMetrics.getHeight();
        g.setColor(active);
        g.drawString("Left-click to move", paneWidth, fontMetrics.getAscent());
        g.drawString("Right-click to interact", paneWidth, fontMetrics.getAscent() + lh);
        String countReport = "Estimated Traffic: " + node.estimatedByteCount + "B (" + (node.estimatedByteCount / 128) + "kbits)";
        g.drawString(countReport, w - fontMetrics.stringWidth(countReport), fontMetrics.getAscent());
        countReport = "Usage: " + (lastByteCount / 128) + "kbits/sec";
        g.drawString(countReport, w - fontMetrics.stringWidth(countReport), fontMetrics.getAscent() + lh);
        Remote[] sremotes = sortRemotes;
        if (sortRemotes == null) {
            ArrayList<Remote> loc = new ArrayList<Remote>(remotes.values());
            for (Map.Entry<String, Entity> key : ents.entrySet()) {
                if (key.getValue().centerX >= paneWidth) {
                    loc.remove(remotes.get(key.getKey()));
                }
            }
            Collections.sort(loc);
            loc.addAll(Arrays.asList(folders));
            for (Folder f : folders) {
                f.contents.clear();
            }
            for (Iterator<Remote> it = loc.iterator(); it.hasNext();) {
                Remote r = it.next();
                r.inFolder = false;
                for (Folder f : folders) {
                    if (f.isInside(r)) {
                        it.remove();
                        if (f.open) {
                            f.place = loc.indexOf(f);
                            f.contents.add(r);
                        }
                    }
                }
            }
            for (Folder f : folders) {
                for (Remote r : f.contents) {
                    r.inFolder = true;
                    loc.add(f.place + 1, r);
                }
            }
            sremotes = loc.toArray(new Remote[loc.size()]);
            sortRemotes = sremotes;
        }
        g.setColor(paneBackground);
        Graphics subscreen = g.create(1, 1, paneWidth - 2, h - 2);
        subscreen.fillRect(0, 0, paneWidth, h);
        if (activeRow != -1) {
            subscreen.setColor(highlight);
            subscreen.fillRect(0, activeRow * rowHeight + currentPaneScroll, paneWidth, rowHeight);
        }
        subscreen.setColor(foreground);
        rowHeight = lh;
        int suby = fontMetrics.getAscent() + currentPaneScroll;
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
        NetworkAutologger.register();
        JFrame frame = new JFrame("Intelligence Panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(640, 480);
        JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JPanel subpanel = new JPanel();
        JScrollPane scroll = new JScrollPane();
        JList lstErrors = new JList();
        final JToggleButton ascroll = new JToggleButton("Autoscroll");
        ascroll.setSelected(true);
        scroll.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (ascroll.isSelected()) {
                    e.getAdjustable().setValue(e.getAdjustable().getMaximum());
                }
            }
        });
        final DefaultListModel dlm = new DefaultListModel();
        lstErrors.setModel(dlm);
        Logger.addTarget(new ListModelLogger(dlm, lstErrors));
        scroll.setViewportView(lstErrors);
        subpanel.setLayout(new BoxLayout(subpanel, BoxLayout.Y_AXIS));
        JPanel btns = new JPanel();
        btns.setLayout(new BoxLayout(btns, BoxLayout.X_AXIS));
        subpanel.add(scroll);
        btns.add(ascroll);
        subpanel.add(btns);
        JButton clear = new JButton("Clear");
        clear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dlm.clear();
            }
        });
        btns.add(clear);
        jsp.setRightComponent(subpanel);
        IPProvider.init();
        jsp.setLeftComponent(new IntelligenceMain(args, CluckGlobals.node, new Ticker(1000)));
        jsp.setDividerLocation(2 * 480 / 3);
        jsp.setResizeWeight(0.7);
        frame.add(jsp);
        frame.setVisible(true);
        Logger.info("Started Poultry Inspector at " + System.currentTimeMillis());
        new PhidgetMonitor().share(CluckGlobals.node);
        CluckGlobals.node.publish("test", new FloatStatus());
        IPProvider.connect();
    }
}
