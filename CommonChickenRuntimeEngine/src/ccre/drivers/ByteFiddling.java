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
package ccre.drivers;

import org.slf4j.LoggerFactory;

/**
 * A collection of useful byte-level fiddling utilities.
 *
 * @author skeggsc
 */
public class ByteFiddling {

    /**
     * Find the first index of the byte b in the byte range, or -1 if it cannot
     * be found in that range.
     *
     * @param bytes the byte array.
     * @param from the start of the range.
     * @param to the end of the range.
     * @param b the byte to look for.
     * @return the index, or -1 if not found.
     */
    public static int indexOf(byte[] bytes, int from, int to, byte b) {
        for (int i = from; i < to; i++) {
            if (bytes[i] == b) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Extracts a subsequence of the bytes.
     *
     * @param bytes the byte array to copy from.
     * @param from the start of the range.
     * @param to the end of the range.
     * @return the bytes from the range.
     */
    public static byte[] sub(byte[] bytes, int from, int to) {
        byte[] out = new byte[to - from];
        System.arraycopy(bytes, from, out, 0, to - from);
        return out;
    }

    /**
     * Parse the ASCII characters in the byte array into an integer.
     *
     * @param bytes the characters to parse.
     * @return the integer, or null if it cannot be parsed.
     */
    public static Integer parseInt(byte[] bytes) {
        return parseInt(bytes, 0, bytes.length);
    }

    /**
     * Parse the ASCII characters in the byte section into an integer.
     *
     * @param bytes the byte array to parse.
     * @param from the start of the byte section.
     * @param to the end of the byte section.
     * @return the integer, or null if it cannot be parsed.
     */
    public static Integer parseInt(byte[] bytes, int from, int to) {
        if (to <= from) {
            return null;
        }
        boolean neg = bytes[from] == '-';
        if (neg) {
            from++;
            if (to <= from) {
                return null;
            }
        }
        int num = 0;
        for (int i = from; i < to; i++) {
            int digit = bytes[i] - '0';
            if (digit < 0 || digit > 9) {
                return null;
            }
            num = (num * 10) + digit;
        }
        return neg ? -num : num;
    }

    /**
     * Counts the number of occurrences of the byte b in the byte array.
     *
     * @param bytes the byte array to search.
     * @param b the byte to look for.
     * @return the number of instances of the byte.
     */
    public static int count(byte[] bytes, byte b) {
        return count(bytes, 0, bytes.length, b);
    }

    /**
     * Counts the number of occurrences of the byte b in the byte section.
     *
     * @param bytes the byte array to search.
     * @param from the start of the byte section.
     * @param to the end of the byte section.
     * @param b the byte to look for.
     * @return the number of instances of the byte.
     */
    public static int count(byte[] bytes, int from, int to, byte b) {
        int count = 0;
        for (int i = from; i < to; i++) {
            if (bytes[i] == b) {
                count++;
            }
        }
        return count;
    }

    /**
     * Split the byte section into multiple byte arrays with b as the delimiter.
     *
     * Will always return 1 + n byte arrays, where n is the number of instances
     * of the byte in the byte section.
     *
     * @param bytes the byte array to search.
     * @param from the start of the byte section.
     * @param to the end of the byte section.
     * @param b the byte to split on.
     * @return the byte arrays.
     */
    public static byte[][] split(byte[] bytes, int from, int to, byte b) {
        byte[][] out = new byte[count(bytes, b) + 1][];
        for (int i = 0; i < out.length; i++) {
            int next = indexOf(bytes, from, to, b);
            out[i] = sub(bytes, from, next);
            from = next + 1;
        }
        return out;
    }

    /**
     * Checks if the byte array (interpreted as ASCII) is the same as the given
     * string.
     *
     * @param a the byte array to compare.
     * @param b the string to compare.
     * @return if the sequences contain the same character data.
     */
    public static boolean streq(byte[] a, String b) {
        int len = a.length;
        if (len != b.length()) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (a[i] != (byte) b.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Parse the ASCII characters in the byte array into a double.
     *
     * @param bytes the byte array to parse.
     * @return the double, or null if it cannot be parsed.
     */
    public static Double parseDouble(byte[] bytes) {
        return parseDouble(bytes, 0, bytes.length);
    }

    /**
     * Parse the ASCII characters in the byte section into a double.
     *
     * @param bytes the byte array to parse.
     * @param from the start of the byte section.
     * @param to the end of the byte section.
     * @return the double, or null if it cannot be parsed.
     */
    public static Double parseDouble(byte[] bytes, int from, int to) {
        try {
            return Double.parseDouble(parseASCII(bytes, from, to));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Parse the ASCII characters in the byte array into a string.
     *
     * @param bytes the byte array to parse.
     * @return the string.
     */
    public static String parseASCII(byte[] bytes) {
        return parseASCII(bytes, 0, bytes.length);
    }

    /**
     * Parse the ASCII characters in the byte section into a string.
     *
     * @param bytes the byte array to parse.
     * @param from the start of the byte section.
     * @param to the end of the byte section.
     * @return the string.
     */
    public static String parseASCII(byte[] bytes, int from, int to) {
        char[] conv = new char[to - from];
        for (int i = from, j = 0; i < to; i++, j++) {
            conv[j] = (char) (bytes[i] & 0xFF);
        }
        return new String(conv);
    }

    /**
     * Encode the byte section into a hexadecimal string.
     *
     * @param bytes the byte array to encode.
     * @param from the start of the byte section.
     * @param to the end of the byte section.
     * @return the hexadecimal version.
     */
    public static String toHex(byte[] bytes, int from, int to) {
        if (to > bytes.length || to < from || from < 0) {
            throw new ArrayIndexOutOfBoundsException("Bad toHex arguments: " + from + ";" + to);
        }
        char[] out = new char[2 * (to - from)];
        for (int i = from, j = 0; i < to; i++) {
            try {
                out[j++] = hex[(bytes[i] >> 4) & 0xF];
                out[j++] = hex[bytes[i] & 0xF];
            } catch (ArrayIndexOutOfBoundsException ex) {
                LoggerFactory.getLogger(ByteFiddling.class).warn("Offending indexes: " + j + "," + i + ": " + from + "," + to + "," + bytes.length + "," + out.length);
                throw ex;
            }
        }
        return new String(out);
    }

    private static final char[] hex = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    /**
     * Converts four little-endian bytes from data into an integer.
     *
     * @param data the data to extract the bytes from
     * @param from the index of the first byte
     * @return the extracted integer.
     */
    public static int asInt32LE(byte[] data, int from) {
        return (data[from] & 0xFF) | ((data[from + 1] & 0xFF) << 8) | ((data[from + 2] & 0xFF) << 16) | ((data[from + 3] & 0xFF) << 24);
    }

    /**
     * Converts four big-endian bytes from data into an integer.
     *
     * @param data the data to extract the bytes from
     * @param from the index of the first byte
     * @return the extracted integer.
     */
    public static int asInt32BE(byte[] data, int from) {
        return ((data[from] & 0xFF) << 24) | ((data[from + 1] & 0xFF) << 16) | ((data[from + 2] & 0xFF) << 8) | (data[from + 3] & 0xFF);
    }
}
