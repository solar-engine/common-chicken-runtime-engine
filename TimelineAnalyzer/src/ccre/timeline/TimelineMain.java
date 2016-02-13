/*
 * Copyright 2016 Cel Skeggs.
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
package ccre.timeline;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

import ccre.log.Logger;
import ccre.recording.Replayer;

/**
 * The launcher for the Timeline system.
 *
 * @author skeggsc
 */
public class TimelineMain extends JFrame {

    /**
     * The main method of the Timeline Inspector.
     *
     * @param args the unused program arguments.
     */
    public static void main(String args[]) {
        Timeline timeline;
        try {
            JFileChooser chooser = new JFileChooser("../SampleRobot/emulator-logs");

            int retval = chooser.showOpenDialog(null);
            if (retval == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try (FileInputStream fis = new FileInputStream(file)) {
                    try (InputStream in = file.getName().endsWith(".gz") ? new GZIPInputStream(fis) : fis) {
                        timeline = new Timeline(new Replayer(in).decode());
                    }
                }
            } else {
                return;
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TimelineMain(timeline).setVisible(true);
            }
        });
    }

    private final TimelinePanel timeline;

    private TimelineMain(Timeline timeline) {
        super("Timeline Inspector");
        this.timeline = new TimelinePanel(timeline);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        this.setContentPane(this.timeline);
        this.setSize(800, 600);
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                try {
                    // nothing needed right now
                } catch (Throwable thr) {
                    Logger.severe("Exception while handling key press", thr);
                }
            }
        });
        this.timeline.start();
    }
}
