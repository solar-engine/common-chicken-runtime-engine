/*
 * Copyright 2013-2014 Colby Skeggs.
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

import ccre.cluck.CluckNode;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

/**
 * A class that takes care of a bunch of different rendering tasks.
 *
 * @author skeggsc
 */
public class Rendering {

    /**
     * The font used for everything.
     */
    public static final Font console = new Font("Monospaced", Font.PLAIN, 11);

    /**
     * Blend the specified colors using the specified fraction. 0 means all
     * color A, 1 means all color B, 0.5 is half-and-half, etc.
     *
     * @param a The first color.
     * @param b The second color.
     * @param f The blending factor.
     * @return The blended color.
     */
    public static Color blend(Color a, Color b, float f) {
        float bpart;
        if (f < 0) {
            bpart = 0;
        } else if (f > 1) {
            bpart = 1;
        } else {
            bpart = f;
        }
        float apart = 1 - bpart;
        return new Color(Math.round(a.getRed() * apart + b.getRed() * bpart), Math.round(a.getGreen() * apart + b.getGreen() * bpart), Math.round(a.getBlue() * apart + b.getBlue() * bpart), Math.round(a.getAlpha() * apart + b.getAlpha() * bpart));
    }

    public static void setupFont(Graphics g) {
        g.setFont(Rendering.console);
    }

    public static int calculateEntityWidth(Graphics g, String path) {
        return Math.max(70, g.getFontMetrics().stringWidth(path) / 2);
    }

    public static int calculateEntityHeight(Graphics g, String path) {
        return g.getFontMetrics().getHeight() * 3 / 2;
    }

    public static void drawEntityBackplate(Graphics g, int centerX, int centerY, int width, int height, Color backgroundColor) {
        g.setColor(Color.BLACK);
        g.fillRect(centerX - width, centerY - height, width * 2, height * 2);
        g.setColor(backgroundColor);
        g.fillRect(centerX - width + 1, centerY - height + 1, width * 2 - 2, height * 2 - 2);
    }

    public static void drawEntityText(Graphics g, int baseX, int baseY, String[] strings) {
        g.setColor(Color.BLACK);
        baseY += g.getFontMetrics().getAscent();
        for (String s : strings) {
            g.drawString(s, baseX, baseY);
            baseY += g.getFontMetrics().getHeight();
        }
    }

    /**
     * Get the color of the remote type.
     *
     * @param rmt The remote type to check the color of.
     * @return The color.
     */
    public static Color getColor(Remote rmt) {
        switch (rmt.type) {
            case CluckNode.RMT_EVENTOUTP:
            case CluckNode.RMT_EVENTINPUT:
                return Color.MAGENTA;
            case CluckNode.RMT_LOGTARGET:
                return Color.RED;
            case CluckNode.RMT_BOOLPROD:
            case CluckNode.RMT_BOOLOUTP:
                return Color.YELLOW;
            case CluckNode.RMT_FLOATPROD:
            case CluckNode.RMT_FLOATOUTP:
                return Color.ORANGE;
            case CluckNode.RMT_OUTSTREAM:
            case CluckNode.RMT_INVOKE:
                return Color.CYAN;
            default:
                return Color.WHITE;
        }
    }

    public static Color floatColorCalculate(float c, Color col) {
        if (c < -1) {
            return Rendering.blend(Color.BLACK, col.darker(), c + 2);
        } else if (c > 1) {
            return Rendering.blend(col.brighter(), Color.WHITE, c - 1);
        } else {
            return Rendering.blend(col.darker(), col.brighter(), (c + 1) / 2);
        }
    }
}
