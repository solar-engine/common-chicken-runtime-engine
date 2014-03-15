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

import ccre.cluck.*;
import ccre.cluck.rpc.RemoteProcedure;
import ccre.concurrency.CollapsingWorkerThread;
import ccre.event.EventConsumer;
import ccre.event.EventSource;
import ccre.log.*;
import ccre.ctrl.ExpirationTimer;
import ccre.ctrl.Ticker;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import javax.swing.*;

/**
 * The upgraded display panel for browsing CCRE networks.
 *
 * @author skeggsc
 */
@SuppressWarnings({"serial", "rawtypes"})
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
    protected final Folder[] folders;
    /**
     * The active dialog, if any.
     */
    protected PoultryDialog dialog;
    /**
     * The list of tabs.
     */
    protected final java.util.List<Tab> tabs;

    /**
     * Create a new Intelligence Panel.
     *
     * @param args The main arguments to the program.
     * @param node The cluck node that this panel will display.
     * @param seconds An event that will be produced every second.
     *
     */
    private IntelligenceMain(String[] args, CluckNode node, EventSource seconds, JButton searcher, JButton reconnector) {
        this.node = node;
        ArrayList<Folder> folderList = new ArrayList<Folder>();
        try {
            File folder = new File(".").getAbsoluteFile();
            File target = null;
            while (folder != null && folder.exists()) {
                target = new File(folder, "poultry-settings.txt");
                if (target.exists() && target.canRead()) {
                    break;
                }
                target = null;
                folder = folder.getParentFile();
            }
            if (target == null) {
                throw new FileNotFoundException("Could not find folders.");
            }
            BufferedReader fin = new BufferedReader(new FileReader(target));
            try {
                while (true) {
                    String line = fin.readLine();
                    if (line == null) {
                        break;
                    }
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    String[] pts = line.split("=", 2);
                    if (pts.length == 1) {
                        throw new IOException("Bad line: no =.");
                    }
                    folderList.add(new Folder(pts[0].trim(), pts[1].trim()));
                }
            } finally {
                fin.close();
            }
        } catch (IOException ex) {
            Logger.log(LogLevel.WARNING, "Could not set up folder list!", ex);
        }
        tabs = Tab.getTabs();
        folders = folderList.toArray(new Folder[folderList.size()]);
        searchLinkName = "big-brother-" + Integer.toHexString(args.hashCode());
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        this.addMouseWheelListener(this);
        final CollapsingWorkerThread discover = new CollapsingWorkerThread("Cluck-Discoverer") {
            @Override
            protected void doWork() {
                IPProvider.connect();
            }
        };
        reconnector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                discover.trigger();
            }
        });
        final CollapsingWorkerThread researcher = new CollapsingWorkerThread("Cluck-Researcher") {
            @Override
            protected void doWork() throws Throwable {
                research();
            }
        };
        searcher.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                researcher.trigger();
            }
        });
        seconds.addListener(new EventConsumer() {
            @Override
            public void eventFired() {
                int cur = IntelligenceMain.this.node.estimatedByteCount;
                lastByteCount = cur - baseByteCount;
                baseByteCount = cur;
            }
        });
        CluckGlobals.node.publish("display-dialog", new RemoteProcedure() {
            @Override
            public void invoke(byte[] in, OutputStream out) {
                if (dialog != null) {
                    try {
                        out.close();
                    } catch (IOException ex) {
                        Logger.log(LogLevel.WARNING, "IOException from return from procedure!", ex);
                    }
                    return;
                }
                dialog = new PoultryDialog(new String(in), out);
            }
        });
        painter.schedule(50, new EventConsumer() {
            @Override
            public void eventFired() {
                repaint();
            }
        });
        this.node.startSearchRemotes(searchLinkName, this);
        painter.start();
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
                Logger.log(LogLevel.WARNING, "Could not set up tab list!", ex);
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
        remotes.clear();
        sortRemotes = null;
        node.cycleSearchRemotes(searchLinkName);
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
                if (key.getValue().centerX >= paneWidth) {
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
        painter.feed();
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        CluckGlobals.ensureInitializedCore();
        //CluckGlobals.node.debugLogAll = true;
        NetworkAutologger.register();
        FileLogger.register();
        JFrame frame = new JFrame("Intelligence Panel");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        final PhidgetMonitor monitor = new PhidgetMonitor();
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                super.windowClosing(we);
                monitor.displayClosing();
                System.exit(0);
            }
        });
        frame.setSize(640, 480);
        if (args.length >= 2) {
            try {
                frame.setSize(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
            } catch (NumberFormatException ex) {
                Logger.log(LogLevel.WARNING, "Bad window position!", ex);
            }
        }
        if (args.length >= 4) {
            try {
                frame.setLocation(Integer.parseInt(args[2]), Integer.parseInt(args[3]));
            } catch (NumberFormatException ex) {
                Logger.log(LogLevel.WARNING, "Bad window position!", ex);
            }
        }
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
        final JButton refresh = new JButton("Refresh");
        btns.add(refresh);
        JButton reconnect = new JButton("Reconnect");
        btns.add(reconnect);
        jsp.setRightComponent(subpanel);
        IPProvider.init();
        jsp.setLeftComponent(new IntelligenceMain(args, CluckGlobals.node, new Ticker(1000), refresh, reconnect));
        jsp.setDividerLocation(2 * frame.getHeight() / 3);
        jsp.setResizeWeight(0.7);
        frame.add(jsp);
        frame.setVisible(true);
        Logger.info("Started Poultry Inspector at " + System.currentTimeMillis());
        final ExpirationTimer ext = new ExpirationTimer();
        ext.schedule(5000, new EventConsumer() {
            @Override
            public void eventFired() {
                Logger.info("Current time: " + new Date());
            }
        });
        ext.schedule(5010, ext.getStopEvent());
        new CluckSubscriber() {
            @Override
            protected void receive(String source, byte[] data) {
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                if (data.length == 1 && data[0] == CluckNode.RMT_NOTIFY) {
                    ext.startOrFeed();
                }
            }
        }.attach(CluckGlobals.node, "notify-fetcher-virt");
        monitor.share(CluckGlobals.node);
        //new VirtualPhidgetDevice().share(CluckGlobals.node);
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                refresh.doClick();
            }
        });
        IPProvider.connect();
        setupWatchdog(monitor);
    }

    private static void setupWatchdog(final PhidgetMonitor monitor) {
        final ExpirationTimer watchdog = new ExpirationTimer();
        watchdog.schedule(500, CluckGlobals.node.subscribeEC("robot/phidget/WatchDog"));
        CluckGlobals.node.publish("WatchDog", new EventConsumer() {
            @Override
            public void eventFired() {
                monitor.connectionUp();
                watchdog.feed();
            }
        });
        watchdog.schedule(2000, new EventConsumer() {
            @Override
            public void eventFired() {
                monitor.connectionDown();
            }
        });
        watchdog.schedule(3000, watchdog.getFeedEvent());
        watchdog.start();
    }
}
