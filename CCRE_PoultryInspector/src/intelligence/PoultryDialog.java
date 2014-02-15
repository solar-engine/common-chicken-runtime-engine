/*
 * Copyright 2014 Colby Skeggs
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

import ccre.log.LogLevel;
import ccre.log.Logger;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A dialog displayed on the Poultry Inspector that's opened by the
 *
 * @author skeggsc
 */
public final class PoultryDialog {

    /**
     * The string that describes the contents of this window.
     */
    private final String[] description;
    /**
     * The number of BUTTON declarations in the description.
     */
    private final int buttonCount;
    /**
     * The positions of the buttons.
     */
    private final int[][] buttonPositions;

    public static final int DEFAULT_WIDTH = 400, DEFAULT_HEIGHT = 300, BUTTON_HEIGHT = 50, BUTTON_PADDING = 25, BUTTONS_PER_LINE = 3;

    public static final Font TITLE_FONT = new Font("Monospaced", Font.PLAIN, 20);
    public static final Font BUTTON_FONT = new Font("Monospaced", Font.PLAIN, 12);
    public static final Font TEXT_FONT = new Font("Monospaced", Font.PLAIN, 12);
    /**
     * The OutputStream to send results to.
     */
    private final OutputStream resultTo;

    /**
     * Create a new Poultry dialog with the specified description string and the
     * specified stream to send the data to.
     *
     * @param description The description of what should be displayed.
     * @param resultTo The stream for outputs to be sent to.
     */
    public PoultryDialog(String description, OutputStream resultTo) {
        this.description = description.split("\r?\n");
        int buttons = 0;
        for (String line : this.description) {
            if (line.startsWith("BUTTON ")) {
                buttons++;
            }
        }
        buttonCount = buttons;
        this.buttonPositions = new int[buttons][];
        this.resultTo = resultTo;
    }

    /**
     * Render the dialog, with the specified pane width, screen size, and
     * graphics pen.
     *
     * @param g The graphics pen.
     * @param basex The pane width. (Start the dialog to the right of this.)
     * @param swidth The screen width.
     * @param sheight The screen height.
     */
    public void render(Graphics g, int basex, int swidth, int sheight) {
        int centerX = (basex + swidth) / 2, centerY = sheight / 2;
        g.setColor(new Color(240, 240, 240));
        g.fillRect(centerX - DEFAULT_WIDTH / 2, centerY - DEFAULT_HEIGHT / 2, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        g.setColor(Color.BLACK);
        g.drawRect(centerX - DEFAULT_WIDTH / 2, centerY - DEFAULT_HEIGHT / 2, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        g.setColor(Color.BLACK);
        Font f = g.getFont();
        int titlePos = centerY - DEFAULT_HEIGHT / 2;
        int btnid = 0;
        for (String line : description) {
            int spt = line.indexOf(' ');
            if (spt == -1) {
                continue;
            }
            String cmd = line.substring(0, spt), arg = line.substring(spt + 1);
            if ("TITLE".equals(cmd)) {
                g.setFont(TITLE_FONT);
                FontMetrics fm = g.getFontMetrics();
                titlePos += fm.getHeight();
                g.setColor(Color.BLACK);
                g.drawString(arg, centerX - fm.stringWidth(arg) / 2, titlePos);
            } else if ("BUTTON".equals(cmd)) {
                g.setFont(BUTTON_FONT);
                FontMetrics fm = g.getFontMetrics();
                int width = (DEFAULT_WIDTH - BUTTON_PADDING) / BUTTONS_PER_LINE;
                g.setColor(Color.YELLOW);
                int cornerULx = centerX - DEFAULT_WIDTH / 2 + BUTTON_PADDING + width * (btnid % 3);
                int cornerULy = centerY + DEFAULT_HEIGHT / 2 - BUTTON_HEIGHT - BUTTON_PADDING;
                cornerULy -= ((int) (btnid / 3)) * (BUTTON_HEIGHT + BUTTON_PADDING);
                g.fillRect(cornerULx, cornerULy, width - BUTTON_PADDING, BUTTON_HEIGHT);
                g.setColor(Color.BLACK);
                g.drawString(arg, cornerULx + (width - BUTTON_PADDING) / 2 - fm.stringWidth(arg) / 2, cornerULy + BUTTON_HEIGHT / 2 - fm.getHeight() / 2 + fm.getAscent());
                buttonPositions[btnid] = new int[]{cornerULx, cornerULy, cornerULx + width - BUTTON_PADDING, cornerULy + BUTTON_HEIGHT};
                btnid++;
            } else if ("TEXT".equals(cmd)) {
                g.setFont(TEXT_FONT);
                FontMetrics fm = g.getFontMetrics();
                titlePos += fm.getHeight();
                g.setColor(Color.BLACK);
                int ai = arg.indexOf(' ');
                int sx = centerX - DEFAULT_WIDTH / 2 + 10, sy = titlePos;
                if (ai != -1) {
                    String[] pts = arg.substring(0, ai).split(",");
                    if (pts.length == 2) {
                        try {
                            sx = Integer.parseInt(pts[0]);
                            sy = Integer.parseInt(pts[1]);
                        } catch (NumberFormatException ex) {
                        }
                    }
                }
                g.drawString(arg, sx, sy);
            }
        }
        g.setFont(f);
    }

    /**
     * Check if a specified position is over the dialog, with the specified pane
     * width and screen size.
     *
     * @param basex The pane width. (Start the dialog to the right of this.)
     * @param swidth The screen width.
     * @param sheight The screen height.
     * @param tx The x-coordinate to check.
     * @param ty The y-coordinate to check.
     * @return If the specified position is over the dialog.
     */
    public boolean isOver(int basex, int swidth, int sheight, int tx, int ty) {
        int centerX = (basex + swidth) / 2, centerY = sheight / 2;
        return centerX - DEFAULT_WIDTH / 2 <= tx && tx <= centerX + DEFAULT_WIDTH / 2
                && centerY - DEFAULT_HEIGHT / 2 <= ty && ty <= centerY + DEFAULT_HEIGHT / 2;
    }

    /**
     * Click on the dialog, with the specified pane width, and screen size.
     * Return whether or not to close the dialog.
     *
     * @param basex The pane width. (Start the dialog to the right of this.)
     * @param swidth The screen width.
     * @param sheight The screen height.
     * @param tx The mouse x-position.
     * @param ty The mouse y-position.
     * @return If the dialog should be closed.
     */
    public boolean press(int basex, int swidth, int sheight, int tx, int ty) {
        String result = null;
        int i=0;
        for (String line : description) {
            if (line.startsWith("BUTTON ")) {
                String arg = line.substring("BUTTON ".length());
                int[] corners = buttonPositions[i++];
                if (corners != null && corners[0] <= tx && tx <= corners[2] && corners[1] <= ty && ty <= corners[3]) {
                    result = arg;
                }
            }
        }
        if (result != null) {
            try {
                resultTo.write(result.getBytes());
                resultTo.close();
            } catch (IOException ex) {
                Logger.log(LogLevel.WARNING, "Cannot return result from Dialog!", ex);
            }
            return true;
        } else {
            return false;
        }
    }
}
