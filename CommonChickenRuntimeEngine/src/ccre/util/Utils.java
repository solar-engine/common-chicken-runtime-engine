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
package ccre.util;

import ccre.chan.FloatInputPoll;
import ccre.ctrl.Mixing;

/**
 * A class for utilities that don't fit anywhere else. Most utilites are in
 * Mixing or CArrayUtils.
 *
 * @see Mixing
 * @see CArrayUtils
 * @author skeggsc
 */
public class Utils {

    private Utils() {
    }

    /**
     * Calculate a value with a deadzone. If the value is within the specified
     * deadzone, the result will be zero instead.
     *
     * @param value the value
     * @param deadzone the deadzone size
     * @return the deadzoned version of the value
     */
    public static float deadzone(float value, float deadzone) {
        return Math.abs(value) > deadzone ? value : 0.0f;
    }
    /**
     * An input representing the current time in seconds. The value is
     * equivalent to
     * <code>System.currentTimeMillis() / 1000.0f</code>
     *
     * @see java.lang.System#currentTimeMillis()
     */
    public static final FloatInputPoll currentTimeSeconds = new FloatInputPoll() {
        public float readValue() {
            return System.currentTimeMillis() / 1000.0f;
        }
    };

    /**
     * Split a string into parts delimited by the specified character.
     *
     * @param s The string to split.
     * @param c The delimiter.
     * @return The parts of the string.
     */
    public static String[] split(String s, char c) {
        int count = 1;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                count++;
            }
        }
        String[] parts = new String[count];
        int last = 0;
        int part = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                parts[part++] = s.substring(last, i);
                last = i + 1;
            }
        }
        parts[part++] = s.substring(last);
        if (part != count) {
            throw new RuntimeException("Internal error - wait, what?");
        }
        return parts;
    }

    /**
     * Run a cycle of ramping code on the previous ramping value, target value,
     * and acceleration limit.
     * 
     * This will return the target value unless it's outside of the limit away from 
     *
     * @param previous
     * @param target
     * @param limit
     * @return
     */
    public static float updateRamping(float previous, float target, float limit) {
        if (limit <= 0) {
            if (limit == 0) {
                return 0;
            }
            limit = -limit;
        }
        if (target > previous + limit) {
            return previous + limit;
        } else if (target < previous - limit) {
            return previous - limit;
        } else {
            return target;
        }
    }
}
