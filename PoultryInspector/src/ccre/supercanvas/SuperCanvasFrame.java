/*
 * Copyright 2013-2016 Cel Skeggs.
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
import java.util.function.Supplier;

import javax.swing.JFrame;

import ccre.log.Logger;

public class SuperCanvasFrame extends JFrame {

    public void start() {
        java.awt.EventQueue.invokeLater(() -> setVisible(true));
    }

    private final SuperCanvasPanel canvas = new SuperCanvasPanel();

    public SuperCanvasFrame(String name, Supplier<? extends SuperCanvasComponent> comp, SuperCanvasComponent... components) {
        super(name);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        this.setContentPane(canvas);
        this.setSize(640, 480);
        Class<? extends SuperCanvasComponent> expected = comp.get().getClass();
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
                        } else if (!canvas.removeAll(expected)) {
                            canvas.add(comp.get());
                        }
                    }
                } catch (Throwable thr) {
                    Logger.severe("Exception while handling key press", thr);
                }
            }
        });
        for (SuperCanvasComponent component : components) {
            canvas.add(component);
        }
        canvas.start();
    }
}
    