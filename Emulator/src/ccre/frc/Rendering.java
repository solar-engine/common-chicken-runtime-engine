/*
 * Copyright 2014-2015 Colby Skeggs
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
package ccre.frc;

import java.awt.Color;
import java.awt.Font;

/**
 * Utilities used for rendering.
 *
 * @author skeggsc
 */
public class Rendering {
    /**
     * The console font. Small and monospaced.
     */
    public static final Font console = new Font("Monospaced", Font.PLAIN, 11);
    /**
     * The error font. Mid-size and monospaced.
     */
    public static final Font error = new Font("Monospaced", Font.PLAIN, 20);
    /**
     * The label font. Reasonably large and monospaced.
     */
    public static final Font labels = new Font("Monospaced", Font.BOLD, 30);

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
}
