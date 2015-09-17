/*
 * Copyright 2015 Colby Skeggs
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
package ccre.util;

import java.util.Random;

import ccre.testing.TestFloatMixing;

public class Values {
    private static final Random random = new Random();

    public static String getRandomString() {
        char[] chrs = new char[random.nextInt(10)];
        for (int i = 0; i < chrs.length; i++) {
            chrs[i] = (char) (random.nextBoolean() ? random.nextBoolean() ? random.nextInt(0xAFFF) : random.nextInt(256) : random.nextInt(33));
        }
        return new String(chrs);
    }

    /**
     * A sequence of interesting floats for testing edge cases: things like
     * negative infinity, NaN, MAX_VALUE, -MAX_VALUE, 0, 1, -1, etc.
     *
     * @see TestFloatMixing#lessInterestingFloats
     */
    public static final float[] interestingFloats = new float[] { Float.NEGATIVE_INFINITY, -Float.MAX_VALUE, -1024.7f, -32f, -6.3f, -1.1f, -1f, -0.7f, -0.5f, -0.3f, -0.1f, -0.001f, -Float.MIN_VALUE, 0, Float.NaN, Float.MIN_VALUE, 0.001f, 0.1f, 0.3f, 0.5f, 0.7f, 1.0f, 1.1f, 6.3f, 32f, 1024.7f, Float.MAX_VALUE, Float.POSITIVE_INFINITY };
    /**
     * A sequence of slightly less interesting floats for testing edge cases:
     * this is like {@link #interestingFloats}, but with only finite values not
     * near MAX_VALUE in magnitude.
     *
     * @see TestFloatMixing#interestingFloats
     */
    public static final float[] lessInterestingFloats = new float[] { -1024.7f, -32f, -6.3f, -1.1f, -1f, -0.7f, -0.5f, -0.3f, -0.1f, -0.001f, -Float.MIN_VALUE, 0, Float.NaN, Float.MIN_VALUE, 0.001f, 0.1f, 0.3f, 0.5f, 0.7f, 1.0f, 1.1f, 6.3f, 32f, 1024.7f };

    public static final boolean[] interestingBooleans = new boolean[] { false, true, true, false, false, true, false, true, false, true, true, true, false, true, false, false, false, true };
}
