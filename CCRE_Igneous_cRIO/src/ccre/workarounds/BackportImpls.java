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
package ccre.workarounds;

import com.sun.squawk.util.Arrays;
import com.sun.squawk.util.Comparer;

/**
 * Methods used for miscellaneous added methods on builtin classes during cRIO
 * backporting.
 * 
 * @author skeggsc
 */
public class BackportImpls {
    /**
     * Convert a boolean to a string, as either "true" or "false".
     * 
     * @param b the boolean to convert.
     * @return "true" or "false" depending on b.
     */
    public static String java_lang_Boolean_toString(boolean b) {
        return b ? "true" : "false";
    }

    /**
     * Convert a string to a boolean. str is interpreted as true iff the value
     * is "true", ignoring case: so, "TRUE", "trUE", "tRuE", and "true" will all
     * be true.
     * 
     * @param str the string to parse.
     * @return the boolean version.
     */
    public static boolean java_lang_Boolean_parseBoolean(String str) {
        return "true".equalsIgnoreCase(str);
    }

    /**
     * Fall-back implementation of initCause. Doesn't actually do anything.
     * 
     * @param athis the "this" throwable.
     * @param thr the throwable.
     * @return the "this" throwable.
     */
    public static Throwable java_lang_Throwable_initCause(Throwable athis, Throwable thr) {
        // Do nothing.
        return athis;
    }

    /**
     * Compare two longs. Returns -1 if a &lt; b, 0 if a == b, and 1 if a &gt;
     * b.
     * 
     * @param a the first long.
     * @param b the second long.
     * @return the comparison result.
     */
    public static int java_lang_Long_compare(long a, long b) {
        return a < b ? -1 : a == b ? 0 : 1;
    }

    /**
     * Implementation of Arrays.sort that actually dispatches to
     * com.sun.squawk.Arrays.sort with an extra Comparer argument to allow the
     * objects to just be Comparables.
     * 
     * @param o the objects to sort.
     */
    public static void java_util_Arrays_sort(Object[] o) {
        Arrays.sort(o, comparableComparer);
    }
    
    private static final Comparer comparableComparer = new Comparer() {
        @SuppressWarnings("unchecked")
        public int compare(Object o1, Object o2) {
            return ((Comparable<Object>) o1).compareTo(o2);
        }
    };
}
