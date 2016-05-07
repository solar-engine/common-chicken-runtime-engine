/*
 * Copyright 2013-2015 Cel Skeggs, 2016 Alexander Mackworth
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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import ccre.verifier.FlowPhase;
import ccre.verifier.IgnoredPhase;

/**
 * A class for utilities that don't fit anywhere else.
 *
 * @author skeggsc
 */
public class Utils {

    /**
     * Calculate a value with a deadzone. If the value is within the specified
     * deadzone, the result will be zero instead.
     *
     * The result is undefined if deadzone is negative, NaN, or infinite.
     *
     * @param value the value
     * @param deadzone the deadzone size
     * @return the deadzoned version of the value
     */
    @FlowPhase
    public static float deadzone(float value, float deadzone) {
        return Math.abs(value) >= deadzone ? value : Float.isNaN(value) ? Float.NaN : 0.0f;
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
    @FlowPhase
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
     * Extracts the big-endian integer starting at offset from array. This is
     * equivalent to:
     * <code>((array[offset] &amp; 0xff) &lt;&lt; 24) | ((array[offset+1] &amp; 0xff) &lt;&lt; 16) | ((array[offset+2] &amp; 0xff) &lt;&lt; 8) | (array[offset+3] &amp; 0xff)</code>
     *
     * @param array The array to extract data from.
     * @param offset The offset in the array of the most significant byte.
     * @return The integer extracted from the array.
     */
    @IgnoredPhase
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
    @IgnoredPhase
    public static float bytesToFloat(byte[] array, int offset) {
        return Float.intBitsToFloat(Utils.bytesToInt(array, offset));
    }

    private Utils() {
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
    @FlowPhase
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
    @IgnoredPhase
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

    /**
     * Convert <code>string</code> to UTF-8 bytes.
     *
     * This method is useful so that one doesn't have to handle throwing of
     * {@link UnsupportedEncodingException}, which should never practically
     * happen.
     *
     * @param string the string to convert.
     * @return the UTF-8 bytes for <code>string</code>.
     */
    @IgnoredPhase
    public static byte[] getBytes(String string) {
        try {
            return string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            // http://stackoverflow.com/questions/6030059/url-decoding-unsupportedencodingexception-in-java
            throw new AssertionError("UTF-8 is unknown", e);
        }
    }

    /**
     * Convert UTF-8 bytes to a String.
     *
     * This method is useful so that one doesn't have to handle throwing of
     * {@link UnsupportedEncodingException}, which should never practically
     * happen.
     *
     * @param data the UTF-8 bytes to convert.
     * @param offset the location in <code>bytes</code> to start at.
     * @param count the number of bytes to process.
     * @return the UTF-8 bytes for <code>string</code>.
     */
    @IgnoredPhase
    public static String fromBytes(byte[] data, int offset, int count) {
        try {
            return new String(data, offset, count, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 is unknown", e);
        }
    }

    /**
     * Join the strings in <code>strings</code> together in sequence with
     * <code>separator</code> between them.
     *
     * For example, <code>["abc", "def", "hij"]</code> joined with
     * <code>"EJ"</code> as the separator would yield
     * <code>"abcEJdefEJhij"</code>.
     *
     * @param strings the strings to join together
     * @param separator the separator to include
     * @return the joined strings
     */
    public static String joinStrings(List<String> strings, String separator) {
        if (strings == null || separator == null) {
            throw new NullPointerException();
        }
        if (strings.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder(strings.get(0));
        for (String element : strings.subList(1, strings.size())) {
            if (element == null) {
                throw new NullPointerException();
            }
            builder.append(separator);
            builder.append(element);
        }
        return builder.toString();
    }

    /**
     * Convert an InputStream that may include carriage returns into an
     * InputStream that does not. This is often useful for converting from
     * windows-style line endings to unix-style line endings.
     *
     * @param in the InputStream to process
     * @return the processed InputStream.
     */
    public static InputStream stripCarriageReturns(InputStream in) {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                int b = in.read();
                while (b == '\r') {
                    b = in.read();
                }
                return b;
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                byte[] temp = new byte[len];
                int length = in.read(temp);
                if (length == -1) {
                    return -1;
                }
                int out = off;
                for (int i = 0; i < length; i++) {
                    if (temp[i] != '\r') {
                        b[out++] = temp[i];
                    }
                }
                return out;
            }

            @Override
            public void close() throws IOException {
                in.close();
            }

            @Override
            public int read(byte[] b) throws IOException {
                return this.read(b, 0, b.length);
            }
        };
    }
}
