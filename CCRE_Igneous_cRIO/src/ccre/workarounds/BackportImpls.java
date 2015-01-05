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
package ccre.workarounds;

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
}
