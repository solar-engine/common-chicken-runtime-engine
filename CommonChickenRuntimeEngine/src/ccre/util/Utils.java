/*
 * Copyright 2013-2014 Colby Skeggs
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
     * An input representing the current time in seconds since a constant but
     * arbitrary point in the past. The value is equivalent to
     * <code>System.currentTimeMillis() / 1000.0f - N</code> where N is some
     * point since when the program was started.
     *
     * @see java.lang.System#currentTimeMillis()
     */
    public static final FloatInputPoll currentTimeSeconds = new FloatInputPoll() {
        private final long base = System.currentTimeMillis();

        public float readValue() {
            return (System.currentTimeMillis() - base) / 1000.0f;
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
     * This will return the target value unless it's outside of the limit away
     * from the previous ramping value, in which case it will be as close as
     * possible.
     *
     * @param previous The previous ramping value.
     * @param target The target value.
     * @param limit The acceleration limit.
     * @return The new value from the ramping cycle
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

    /**
     * Dynamically cast the specified object to the specified class.
     *
     * @param <T> The type to cast to.
     * @param o The object to cast.
     * @param clazz The class to cast to.
     * @return The newly casted object.
     * @throws ClassCastException If the specified object cannot be cast to the
     * specified class.
     */
    @SuppressWarnings("unchecked")
    public static <T> T dynamicCast(Object o, Class<T> clazz) throws ClassCastException {
        if (clazz.isInstance(o)) {
            return (T) o;
        } else {
            throw new ClassCastException("Cannot cast to " + clazz + "!");
        }
    }

    /**
     * Get the class of the specified object, specified as descended from a
     * specified type. Used to get around unchecked warnings.
     *
     * @param <T> The base type for the object.
     * @param obj The object to get the class of.
     * @return The class of the object.
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getGenericClass(T obj) {
        return (Class<T>) obj.getClass();
    }
    
    /**
     * Extracts the big-endian integer starting at offset from array. This is equivalent to:
     * <code>((array[offset] & 0xff) << 24) | ((array[offset+1] & 0xff) << 16) | ((array[offset+2] & 0xff) << 8) | (array[offset+3] & 0xff)</code>
     * @param array The array to extract data from.
     * @param offset The offset in the array of the most significant byte.
     * @return The integer extracted from the array.
     */
    public static int bytesToInt(byte[] array, int offset) {
        int highWord = ((array[offset] & 0xff) << 24) | ((array[offset+1] & 0xff) << 16);
        int lowWord = ((array[offset+2] & 0xff) << 8) | (array[offset+3] & 0xff);
        return highWord | lowWord;
    }
    
    /**
     * Extracts the floating-point number starting at offset from array. This is equivalent to:
     * <code>Float.intBitsToFloat(Utils.bytesToInt(array, offset))</code>
     * @param array The array to extract data from.
     * @param offset The offset in the array of the most significant byte of the intermediate integer.
     * @return The float extracted from the array.
     */
    public static float bytesToFloat(byte[] array, int offset) {
        return Float.intBitsToFloat(Utils.bytesToInt(array, offset));
    }
}
