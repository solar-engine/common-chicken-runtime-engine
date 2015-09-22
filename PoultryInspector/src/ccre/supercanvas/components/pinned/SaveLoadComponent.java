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
package ccre.supercanvas.components.pinned;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccre.supercanvas.Rendering;
import ccre.supercanvas.SuperCanvasComponent;

/**
 * A component that always displays in a fixed position, allowing for saving and
 * loading of layouts of the canvas.
 *
 * @author skeggsc
 */
public class SaveLoadComponent extends SuperCanvasComponent {
    private static Logger logger = LoggerFactory.getLogger(SaveLoadComponent.class);

    private static final long serialVersionUID = -8609998417324680908L;
    private final int x, y;
    private int width = 10, height = 10, btnBorder = 5;
    private transient JFileChooser chooser;

    /**
     * Create a new SaveLoadComponent.
     *
     * @param x the X-coordinate.
     * @param y the Y-coordinate.
     */
    public SaveLoadComponent(int x, int y) {
        super(true);
        this.x = x;
        this.y = y;
    }

    @Override
    public void render(Graphics2D g, int screenWidth, int screenHeight, FontMetrics fontMetrics, int mouseX, int mouseY) {
        width = 4 + Math.max(fontMetrics.stringWidth("Save"), fontMetrics.stringWidth("Load"));
        height = 4 + fontMetrics.getHeight() * 2;
        Rendering.drawBody(Color.YELLOW, g, x + width / 2, y + height / 2, width, height);
        g.setColor(Color.BLACK);
        g.drawString("Save", x + 2, y + 2 + fontMetrics.getAscent());
        btnBorder = y + fontMetrics.getHeight();
        g.drawString("Load", x + 2, y + 2 + fontMetrics.getAscent() + fontMetrics.getHeight());
    }

    @Override
    public boolean contains(int tx, int ty) {
        return tx >= this.x && tx < this.x + width && ty >= this.y && ty < this.y + height;
    }

    @Override
    public boolean onInteract(int tx, int ty) {
        if (ty < btnBorder) {
            logger.info("Saving...");
            try {
                saveLayout();
            } catch (IOException ex) {
                logger.error("Could not save!", ex);
            }
        } else {
            logger.info("Loading...");
            try {
                loadLayout();
            } catch (ClassNotFoundException ex) {
                logger.error("Could not load!", ex);
            } catch (IOException ex) {
                logger.error("Could not load!", ex);
            }
        }
        return true;
    }

    private JFileChooser getChooser() {
        if (chooser == null) {
            chooser = new JFileChooser(".");
            chooser.setFileFilter(new FileNameExtensionFilter("Saved Layout", "ser"));
        }
        return chooser;
    }

    private void saveLayout() throws IOException, FileNotFoundException {
        int retval = getChooser().showSaveDialog(getPanel());
        if (retval == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().contains(".") && chooser.getFileFilter() instanceof FileNameExtensionFilter) {
                file = new File(file.getParentFile(), file.getName() + "." + ((FileNameExtensionFilter) chooser.getFileFilter()).getExtensions()[0]);
            }
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
            try {
                getPanel().save(out);
            } finally {
                out.close();
            }
            logger.info("Saved as " + file + ".");
        } else {
            logger.info("Cancelled by user.");
        }
    }

    private void loadLayout() throws ClassNotFoundException, IOException, FileNotFoundException {
        int retval = getChooser().showOpenDialog(getPanel());
        if (retval == JFileChooser.APPROVE_OPTION) {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(chooser.getSelectedFile()));
            try {
                getPanel().load(in);
            } finally {
                in.close();
            }
            logger.info("Loaded from " + chooser.getSelectedFile() + ".");
        } else {
            logger.info("Cancelled by user.");
        }
    }

    @Override
    public boolean onSelect(int tx, int ty) {
        return onInteract(tx, ty);
    }
}
