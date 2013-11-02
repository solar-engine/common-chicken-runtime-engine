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

import ccre.chan.BooleanInput;
import ccre.chan.BooleanOutput;
import ccre.chan.BooleanStatus;
import ccre.chan.FloatInput;
import ccre.chan.FloatInputProducer;
import ccre.chan.FloatOutput;
import ccre.chan.FloatStatus;
import ccre.cluck.CluckGlobals;
import ccre.cluck.CluckNode;
import ccre.cluck.CluckRemoteListener;
import ccre.concurrency.CollapsingWorkerThread;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;

public class IntelligenceMain extends JPanel implements CluckRemoteListener, MouseMotionListener, MouseWheelListener, MouseListener {

    public static final Color canvasBackground = Color.WHITE;
    public static final Color paneBackground = Color.YELLOW;
    public static final Color highlight = Color.ORANGE;
    public static final Color active = Color.RED;
    public static final Color foreground = Color.BLACK;
    public static final Font console = new Font("Monospaced", Font.PLAIN, 11);
    public static final int paneWidth = 256;
    public final CluckNode node;
    protected int activeRow = -1, rowHeight = -1;
    protected final String lrecv;
    protected final HashMap<String, Remote> remotes = new HashMap<String, Remote>();
    protected Remote[] sortRemotes = null;
    protected final LinkedHashMap<String, Entity> ents = new LinkedHashMap<String, Entity>();
    protected Entity activeEntity = null;
    protected int relActiveX, relActiveY, mouseBtn;
    protected final ExpirationTimer painter = new ExpirationTimer();
    protected int baseByteCount = 0, lastByteCount = 0;
    protected int currentPaneScroll = 0;

    private IntelligenceMain(String[] args, CluckNode node, EventSource seconds) {
        this.node = node;
        lrecv = "big-brother-" + Integer.toHexString(args.hashCode());
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
                research(); // WORKING HERE ... about to finish and test
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
        this.node.startSearchRemotes(lrecv, this);
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
                    Thread.sleep(10);
                    if (now > last + 1000) {
                        if (now > last + 5000) {
                            last = now;
                        }
                        last += 1000;
                        prod.produce();
                    }
                }
            }
        }.start();
        CluckGlobals.node.publish("time", (FloatInputProducer) time);
        CluckGlobals.node.publish("test", new EventLogger(LogLevel.INFO, "Test."));
        CluckGlobals.node.publish("ticker", (EventSource) prod);
        BooleanStatus immd = new BooleanStatus();
        CluckGlobals.node.publish("immd-in", (BooleanInput) immd);
        CluckGlobals.node.publish("immd-out", (BooleanOutput) immd);
        FloatStatus immf = new FloatStatus();
        CluckGlobals.node.publish("immf-in", (FloatInput) immf);
        CluckGlobals.node.publish("immf-out", (FloatOutput) immf);
        NetworkAutologger.register();
        JFrame frame = new JFrame("Intelligence Panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(640, 480);
        JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JPanel subpanel = new JPanel();
        JScrollPane scroll = new JScrollPane();
        JList lstErrors = new JList();
        final JToggleButton ascroll = new JToggleButton("Autoscroll");
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
        LoggingTarget lt = new ListModelLogger(dlm, lstErrors);
        Logger.target = new MultiTargetLogger(Logger.target, lt);
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
        //PhidgetMonitor pmon = new PhidgetMonitor();
        //pmon.share(CluckGlobals.node);
        //JoystickMonitor jmon = new JoystickMonitor(1);
        //jmon.share(CluckGlobals.node);
        jsp.setLeftComponent(new IntelligenceMain(args, CluckGlobals.node, prod));
        jsp.setDividerLocation(2 * 480 / 3);
        jsp.setResizeWeight(0.7);
        frame.add(jsp);
        frame.setVisible(true);
        Logger.info("Started Poultry Inspector at " + System.currentTimeMillis());
        IPProvider.connect();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        currentPaneScroll -= e.getWheelRotation() * 2;
        if (currentPaneScroll > 0) {
            currentPaneScroll = 0;
        }
    }
}
