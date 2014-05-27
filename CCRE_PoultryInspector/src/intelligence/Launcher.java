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
import ccre.cluck.CluckNode;
import ccre.cluck.CluckSubscriber;
import ccre.ctrl.ExpirationTimer;
import ccre.ctrl.Ticker;
import ccre.log.FileLogger;
import ccre.log.Logger;
import ccre.log.NetworkAutologger;
import ccre.net.CountingNetworkProvider;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

/**
 * The main launcher class that sets up the Poultry Inspector.
 *
 * @author skeggsc
 */
public class Launcher {

    public static void main(String[] args) {
        // Done
        CountingNetworkProvider.register();
        NetworkAutologger.register();
        FileLogger.register();
        JFrame frame = new JFrame("Intelligence Panel");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        final IPhidgetMonitor monitor = args.length > 0 && "-virtual".equals(args[0]) ? new VirtualPhidgetMonitor() : args.length > 0 && "-phidget".equals(args[0]) ? new PhidgetMonitor() : new NonexistentPhidgetMonitor();
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                monitor.displayClosing();
                System.exit(0);
            }
        });
        frame.setSize(640, 480);
        if (args.length >= 2) {
            try {
                frame.setSize(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
            } catch (NumberFormatException ex) {
                Logger.warning("Bad window position!", ex);
            }
        }
        if (args.length >= 4) {
            try {
                frame.setLocation(Integer.parseInt(args[2]), Integer.parseInt(args[3]));
            } catch (NumberFormatException ex) {
                Logger.warning("Bad window position!", ex);
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
        final JTextField forcedAddress = new JTextField("*");
        forcedAddress.setFont(new Font("Monospaced", 0, 12));
        btns.add(forcedAddress);
        JButton setAddress = new JButton("Set Address");
        setAddress.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IPProvider.forcedAddress = forcedAddress.getText();
            }
        });
        btns.add(setAddress);
        jsp.setRightComponent(subpanel);
        IPProvider.init();
        IntelligenceMain m = new IntelligenceMain();
        m.start(new Ticker(1000), refresh, reconnect);
        jsp.setLeftComponent(m);
        jsp.setDividerLocation(2 * frame.getHeight() / 3);
        jsp.setResizeWeight(0.7);
        frame.add(jsp);
        frame.setVisible(true);
        Logger.info("Started Poultry Inspector at " + System.currentTimeMillis());
        final ExpirationTimer ext = new ExpirationTimer();
        ext.schedule(5000, new EventOutput() {
            @Override
            public void event() {
                Logger.info("Current time: " + new Date());
            }
        });
        ext.schedule(5010, ext.getStopEvent());
        new CluckSubscriber(Cluck.getNode()) {
            @Override
            protected void receive(String source, byte[] data) {
            }

            @Override
            protected void receiveBroadcast(String source, byte[] data) {
                if (data.length == 1 && data[0] == CluckNode.RMT_NOTIFY) {
                    ext.startOrFeed();
                }
            }
        }.attach("notify-fetcher-virt");
        monitor.share();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                refresh.doClick();
            }
        });
        IPProvider.connect();
        setupWatchdog(monitor);
    }

    private static void setupWatchdog(final IPhidgetMonitor monitor) {
        final ExpirationTimer watchdog = new ExpirationTimer();
        watchdog.schedule(500, Cluck.subscribeEO("robot/phidget/WatchDog"));
        Cluck.publish("WatchDog", new EventOutput() {
            @Override
            public void event() {
                monitor.connectionUp();
                watchdog.feed();
            }
        });
        watchdog.schedule(2000, new EventOutput() {
            @Override
            public void event() {
                monitor.connectionDown();
            }
        });
        watchdog.schedule(3000, watchdog.getFeedEvent());
        watchdog.start();
    }

}
