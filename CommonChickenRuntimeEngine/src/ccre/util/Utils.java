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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * A class for utilities that don't fit anywhere else. Most utilites are in
 * Mixing or CArrayUtils.
 *
 * @see Mixing
 * @see CArrayUtils
 * @author skeggsc
 */
public class Utils {

    private static final long baseMeasurementTime = System.currentTimeMillis();

    /**
     * Get the current time in seconds since a constant but arbitrary point in
     * the past. The value is equivalent to
     * <code>System.currentTimeMillis() / 1000.0f - N</code> where N is some
     * point since when the program was started.
     *
     * @return the current time in seconds.
     *
     * @see java.lang.System#currentTimeMillis()
     * @see #currentTimeSeconds has the same value.
     */
    public static float getCurrentTimeSeconds() {
        return (System.currentTimeMillis() - baseMeasurementTime) / 1000.0f;
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
     * If the ramping constant is zero, no ramping will be applied - the input
     * will be copied to the result.
     *
     * @param previous The previous ramping value.
     * @param target The target value.
     * @param limit The acceleration limit.
     * @return The new value from the ramping cycle
     */
    public static float updateRamping(float previous, float target, float limit) {
        float reallimit;
        if (limit <= 0) {
            if (limit == 0) {
                return target;
            }
            reallimit = -limit;
        } else {
            reallimit = limit;
        }
        if (target > previous + reallimit) {
            return previous + reallimit;
        } else if (target < previous - reallimit) {
            return previous - reallimit;
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
     * @deprecated Not used anywhere in the CCRE, and not particularly useful,
     * so becoming deprecated. Copy this into your own code if you still need
     * it.
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public static <T> T dynamicCast(Object o, Class<T> clazz) throws ClassCastException {
        if (!clazz.isInstance(o)) {
            throw new ClassCastException("Cannot cast to " + clazz + "!");
        }
        return (T) o;
    }

    /**
     * Get the class of the specified object, specified as descended from a
     * specified type. Used to get around unchecked warnings.
     *
     * @param <T> The base type for the object.
     * @param obj The object to get the class of.
     * @return The class of the object.
     * @deprecated Not used anywhere in the CCRE, and not particularly useful,
     * so becoming deprecated. Copy this into your own code if you still need
     * it.
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public static <T> Class<T> getGenericClass(T obj) {
        return (Class<T>) obj.getClass();
    }

    /**
     * Extracts the big-endian integer starting at offset from array. This is
     * equivalent to:
     * <code>((array[offset] &amp; 0xff) &lt;&lt; 24) | ((array[offset+1] &amp; 0xff) &lt;&lt; 16) | ((array[offset+2] &amp; 0xff) &lt;&lt; 8) | (array[offset+3] &amp; 0xff)</code>
     *
     * @param array The array to extract data from.
     * @param offset The offset in the array of the most significant byte.
     * @return The integer extracted from the array.
     */
    public static int bytesToInt(byte[] array, int offset) {
        int highWord = ((array[offset] & 0xff) << 24) | ((array[offset + 1] & 0xff) << 16);
        int lowWord = ((array[offset + 2] & 0xff) << 8) | (array[offset + 3] & 0xff);
        return highWord | lowWord;
    }

    /**
     * Extracts the floating-point number starting at offset from array. This is
     * equivalent to:
     * <code>Float.intBitsToFloat(Utils.bytesToInt(array, offset))</code>
     *
     * @param array The array to extract data from.
     * @param offset The offset in the array of the most significant byte of the
     * intermediate integer.
     * @return The float extracted from the array.
     */
    public static float bytesToFloat(byte[] array, int offset) {
        return Float.intBitsToFloat(Utils.bytesToInt(array, offset));
    }

    /**
     * This is a replacement for String.isEmpty() for downgrading.
     *
     * @param str The string to check the length of.
     * @return if the specified string is empty.
     */
    public static boolean isStringEmpty(String str) {
        return str.length() == 0;
    }

    /**
     * This is a replacement for String.contains(String) for downgrading.
     *
     * @param outer The haystack to search.
     * @param inner The needle to search for.
     * @return if the inner string is found in the outer string.
     */
    public static boolean doesStringContain(String outer, String inner) {
        int ind = outer.indexOf(inner);
        return ind != -1;
    }

    private Utils() {
    }

    public static void checkNull(Object... objs) {
        for (Object obj : objs) {
            if (obj == null) {
                throw new NullPointerException();
            }
        }
    }

    /**
     * Convert the specified Throwable to a String that contains what would have
     * been printed by printThrowable.
     *
     * Printing this value is equivalent to just calling printThrowable
     * originally.
     *
     * @param thr the throwable to print.
     * @return the String version of the throwable, including the traceback, or
     * null if the throwable was null.
     */
    public static String toStringThrowable(Throwable thr) {
        if (thr == null) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        thr.printStackTrace(new PrintStream(out));
        return out.toString();
    }

    /**
     * Get diagnostic information for someone in the call stack of this method,
     * at a given index.
     *
     * Index 0 is the caller of this method; 1 is the caller of that method,
     * etc.
     *
     * This should contain, at the very least, the class, but should also
     * contain the method, source file, and line number if possible.
     *
     * @param index which frame to report.
     * @return a CallerInfo for the specified caller, or null.
     */
    public static CallerInfo getMethodCaller(int index) {
        int traceIndex = index + 1;
        StackTraceElement[] trace = new Throwable().getStackTrace();
        if (traceIndex <= 0 || traceIndex >= trace.length || trace[traceIndex] == null) {
            return null;
        } else {
            StackTraceElement elem = trace[traceIndex];
            return new CallerInfo(elem.getClassName(), elem.getMethodName(), elem.getFileName(), elem.getLineNumber());
        }
    }
}
