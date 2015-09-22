/*
 * Copyright 2013-2015 Colby Skeggs.
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
package ccre.supercanvas;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.log.FileLogger;
import ccre.log.NetworkAutologger;
import ccre.net.TrafficCounting;
import ccre.supercanvas.components.LoggingComponent;
import ccre.supercanvas.components.palette.TopLevelPaletteComponent;
import ccre.supercanvas.components.pinned.CluckNetworkingComponent;
import ccre.supercanvas.components.pinned.EditModeComponent;
import ccre.supercanvas.components.pinned.SaveLoadComponent;
import ccre.supercanvas.components.pinned.StartComponent;

/**
 * The launcher for the SuperCanvas system.
 *
 * @author skeggsc
 */
public class SuperCanvasMain extends JFrame {
    private static Logger logger = LoggerFactory.getLogger(SuperCanvasMain.class);

    /**
     * The main method of the Poultry Inspector.
     *
     * @param args the unused program arguments.
     */
    public static void main(String args[]) {
        TrafficCounting.setCountingEnabled(true);
        NetworkAutologger.register();
        FileLogger.register();
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SuperCanvasMain().setVisible(true);
            }
        });
    }

    private static final long serialVersionUID = -4924276427803831926L;

    private final SuperCanvasPanel canvas = new SuperCanvasPanel();

    private SuperCanvasMain() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        this.setContentPane(canvas);
        this.setSize(640, 480);
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                try {
                    if (canvas.editing != null) {
                        char c = e.getKeyChar();
                        if (c >= 32 && c <= 126) {
                            canvas.editing.append(c);
                        } else {
                            switch (e.getKeyCode()) {
                            case KeyEvent.VK_ESCAPE:
                                canvas.editing.setLength(0);
                                break;
                            case KeyEvent.VK_BACK_SPACE:
                                canvas.editing.setLength(Math.max(0, canvas.editing.length() - 1));
                                break;
                            case KeyEvent.VK_END:
                            case KeyEvent.VK_ENTER:
                                canvas.pressedEnter();
                                break;
                            }
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                        if (e.isControlDown()) {
                            canvas.editmode = !canvas.editmode;
                        } else if (!canvas.removeAll(TopLevelPaletteComponent.class)) {
                            canvas.add(new TopLevelPaletteComponent(200, 200));
                        }
                    }
                } catch (Throwable thr) {
                    logger.error("Exception while handling key press", thr);
                }
            }
        });
        canvas.add(new LoggingComponent(312, 300));
        canvas.add(new CluckNetworkingComponent());
        canvas.add(new EditModeComponent());
        canvas.add(new StartComponent());
        canvas.add(new SaveLoadComponent(0, 0));
        canvas.start();
    }
}
