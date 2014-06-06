/*
 * Copyright 2014 Colby Skeggs.
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
package treeframe;

import ccre.log.Logger;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SaveLoadComponent extends SuperCanvasComponent {

    private final int x, y;
    private int width = 10, height = 10, btnBorder = 5;

    public SaveLoadComponent(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        width = 4 + Math.max(fontMetrics.stringWidth("Save"), fontMetrics.stringWidth("Load"));
        height = 4 + fontMetrics.getHeight() * 2;
        g.setPaint(new GradientPaint(x, y, Color.WHITE, x + width, y + height, Color.YELLOW));
        g.fillRoundRect(x, y, width, height, 10, 10);
        g.setColor(Color.BLACK);
        g.drawString("Save", x + 2, y + 2 + fontMetrics.getAscent());
        btnBorder = y + fontMetrics.getHeight();
        g.drawString("Load", x + 2, y + 2 + fontMetrics.getAscent() + fontMetrics.getHeight());
    }

    @Override
    public boolean contains(int x, int y) {
        return x >= this.x && x < this.x + width && y >= this.y && y < this.y + height;
    }

    @Override
    public boolean onInteract(int x, int y) {
        if (y < btnBorder) {
            Logger.info("Saving...");
            try {
                FileOutputStream fout = new FileOutputStream("saved-panel.ser");
                ObjectOutputStream out;
                try {
                    out = new ObjectOutputStream(fout);
                } catch (IOException thr) {
                    try {
                        fout.close();
                    } catch (IOException ex) {
                        thr.addSuppressed(ex);
                    }
                    throw thr;
                }
                try {
                    getPanel().save(out);
                } finally {
                    out.close();
                }
                Logger.info("Saved!");
            } catch (IOException ex) {
                Logger.severe("Could not save!", ex);
            }
        } else {
            Logger.info("Loading...");
            try {
                FileInputStream fin = new FileInputStream("saved-panel.ser");
                ObjectInputStream in;
                try {
                    in = new ObjectInputStream(fin);
                } catch (IOException thr) {
                    try {
                        fin.close();
                    } catch (IOException ex) {
                        thr.addSuppressed(ex);
                    }
                    throw thr;
                }
                try {
                    getPanel().load(in);
                } finally {
                    in.close();
                }
                Logger.info("Loaded!");
            } catch (ClassNotFoundException ex) {
                Logger.severe("Could not load!", ex);
            } catch (IOException ex) {
                Logger.severe("Could not load!", ex);
            }
        }
        return true;
    }

    @Override
    public boolean onSelect(int x, int y) {
        return onInteract(x, y);
    }
}
