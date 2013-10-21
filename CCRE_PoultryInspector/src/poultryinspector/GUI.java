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
package poultryinspector;

import poultryinspector.interactor.Interactor;
import ccre.chan.BooleanInput;
import ccre.chan.BooleanOutput;
import ccre.cluck.CluckEncoder;
import ccre.cluck.CluckEncoder.AvailableListener;
import ccre.cluck.CluckGlobals;
import ccre.cluck.CluckNetworkedClient;
import ccre.concurrency.CollapsingWorkerThread;
import ccre.log.LogLevel;
import ccre.log.Logger;
import ccre.log.MultiTargetLogger;
import java.awt.Color;
import java.awt.EventQueue;
import java.io.IOException;
import javax.swing.DefaultListModel;
import poultryinspector.controller.JoystickMonitor;

/**
 * The main class of PoultyInspector. This contais the frame that everything
 * starts from.
 *
 * @author skeggsc
 */
public class GUI extends javax.swing.JFrame {

    /**
     * The color to display when something is false.
     */
    public static final Color colFalse = new Color(255, 0, 0);
    /**
     * The color to display when something is true.
     */
    public static final Color colTrue = new Color(0, 192, 0);
    /**
     * The list of detected objects on the network.
     */
    private final DefaultListModel<String> detectedObjects = new DefaultListModel<String>();
    /**
     * The listing of errors that the logger has received.
     */
    private final DefaultListModel<Object> errorListing = new DefaultListModel<Object>();
    /**
     * The listener that waits for new objects to be discovered.
     */
    protected AvailableListener availListen = new AvailableListener() {
        @Override
        protected void updatedReceived() {
        }

        @Override
        protected void updateAdded(final String string) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    detectedObjects.addElement(string);
                }
            });
        }
    };
    /**
     * The PhidgetMonitor that allows for the phidget to be shared over the
     * network.
     */
    public PhidgetMonitor phidget = null;//new PhidgetMonitor();
    /**
     * The JoystickMonitor that allows for a joystick to be shared over the
     * network. It will reference the first joystick found that is plugged in.
     */
    public JoystickMonitor joystick = new JoystickMonitor(1);
    /**
     * The worker thread that is used to reconnect to the robot (or other cluck
     * server).
     */
    public CollapsingWorkerThread reconnect = new CollapsingWorkerThread("Cluck-Reconnector") {
        @Override
        protected void doWork() {
            startConnection();
        }
    };

    /**
     * Create the GUI.
     */
    private GUI() {
        CluckGlobals.ensureInitializedCore();
        initComponents();
        this.setTitle("Poultry Inspector");
        reconnect.getRunningStatus().addTarget(new BooleanOutput() {
            @Override
            public void writeValue(final boolean bln) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        btnReconnect.setText(bln ? "Reconnecting..." : "Reconnect");
                    }
                });
            }
        });
        Logger.target = new MultiTargetLogger(Logger.target, new ListModelLogger(errorListing, lstErrors));
        CluckEncoder encoder = CluckGlobals.encoder;
        encoder.searchAndSubscribePublished(availListen);
        IPProvider.forcedAddress.whenModified(reconnect);
        encoder.publishLoggingTarget("general-logger", LogLevel.FINEST, Logger.target);
        encoder.publishStringHolder("CluckClient.robotAddress", IPProvider.forcedAddress);
        BooleanInput binp = encoder.subscribeBooleanInputProducer("phidget-attached", false);
        binp.addTarget(new BooleanOutput() {
            @Override
            public void writeValue(boolean bln) {
                labPhidget.setForeground(bln ? colTrue : colFalse);
            }
        });
        if (phidget != null) {
            phidget.share(encoder);
        }
        if (joystick != null && joystick.isConnected()) {
            joystick.share(encoder);
        }
    }

    /**
     * Connect to the server.
     */
    private void startConnection() {
        try {
            IPProvider.connect();
            if (CluckGlobals.cli != null) {
                Logger.finer("Connected!");
            }
        } catch (IOException ex) {
            if (CluckGlobals.cli != null) { // TODO: Move this to CluckGlobals
                CluckGlobals.cli.stopClient();
                CluckGlobals.cli = null;
            }
            Logger.log(LogLevel.WARNING, "Could not connect!", ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        btnInteract = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        labPhidget = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        lstCluck = new javax.swing.JList();
        btnReconnect = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        btnDebugTools = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        lstErrors = new javax.swing.JList();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jSplitPane1.setDividerLocation(300);
        jSplitPane1.setPreferredSize(new java.awt.Dimension(600, 545));

        btnInteract.setText("Interact");
        btnInteract.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInteractActionPerformed(evt);
            }
        });

        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });

        labPhidget.setForeground(new java.awt.Color(255, 0, 0));
        labPhidget.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labPhidget.setText("PHIDGET");

        lstCluck.setModel(detectedObjects);
        jScrollPane2.setViewportView(lstCluck);

        btnReconnect.setText("Reconnect");
        btnReconnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReconnectActionPerformed(evt);
            }
        });

        btnClear.setText("Clear");
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });

        btnDebugTools.setText("Debug Tools");
        btnDebugTools.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDebugToolsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(btnInteract)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRefresh)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labPhidget)
                .addGap(0, 104, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(btnReconnect, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnClear)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDebugTools, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnReconnect)
                    .addComponent(btnClear)
                    .addComponent(btnDebugTools))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnInteract)
                    .addComponent(btnRefresh)
                    .addComponent(labPhidget, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jSplitPane1.setLeftComponent(jPanel1);

        lstErrors.setModel(errorListing);
        jScrollPane3.setViewportView(lstErrors);

        jSplitPane1.setRightComponent(jScrollPane3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1044, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnReconnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReconnectActionPerformed
        reconnect.trigger();
    }//GEN-LAST:event_btnReconnectActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        errorListing.removeAllElements();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnInteractActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInteractActionPerformed
        Interactor.interact((String) lstCluck.getSelectedValue());
    }//GEN-LAST:event_btnInteractActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        detectedObjects.removeAllElements();
        availListen.clearRecord();
        CluckGlobals.encoder.searchPublished();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnDebugToolsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDebugToolsActionPerformed
        new DebugToolsForm().setVisible(true);
    }//GEN-LAST:event_btnDebugToolsActionPerformed

    /**
     * The entry point for the Poultry Inspector.
     *
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            Logger.log(LogLevel.WARNING, "Could not set look and feel.", ex);
        } catch (InstantiationException ex) {
            Logger.log(LogLevel.WARNING, "Could not set look and feel.", ex);
        } catch (IllegalAccessException ex) {
            Logger.log(LogLevel.WARNING, "Could not set look and feel.", ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            Logger.log(LogLevel.WARNING, "Could not set look and feel.", ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GUI().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnDebugTools;
    private javax.swing.JButton btnInteract;
    private javax.swing.JButton btnReconnect;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JLabel labPhidget;
    private javax.swing.JList lstCluck;
    private javax.swing.JList lstErrors;
    // End of variables declaration//GEN-END:variables
}
