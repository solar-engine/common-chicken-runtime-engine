/*
 * Copyright 2015 Cel Skeggs
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

import java.io.IOException;

/**
 * A parser for NMEA strings. Not currently tested - it was planned for use with
 * the UM7LT but wasn't eventually used.
 *
 * @author skeggsc
 */
public class NMEA {

    /**
     * Find the next end of a NMEA packet. (A CR-LN sequence.)
     *
     * @param bytes the byte array to search.
     * @param from the start of the section to search.
     * @param to the end of the section to search.
     * @return the index directly after the newline, or -1 if the packet end
     * cannot be found.
     */
    public static int getPacketEnd(byte[] bytes, int from, int to) {
        while (from < to) {
            int cr = ByteFiddling.indexOf(bytes, from, to - 1, (byte) '\r');
            if (cr == -1) {
                break;
            }
            if (bytes[cr + 1] == '\n') {
                return cr + 2;
            }
            from = cr + 1;
        }
        return -1;
    }

    /**
     * This parses out an NMEA packet into a series of sections.
     *
     * This is from the UM7LT. If this is wrong for another device, perhaps we
     * need multiple versions.
     *
     * @param nmea the NMEA string's byte array.
     * @param from the start of the byte section.
     * @param to the end of the byte section.
     * @return an array of the parsed-out chunks.
     * @throws IOException if the NMEA frame is malformed.
     */
    public static byte[][] parse(byte[] nmea, int from, int to) throws IOException {
        if (to - from < 3 || nmea[from] != '$' || nmea[to - 2] != '\r' || nmea[to - 1] != '\n') {
            throw new IOException("NMEA frame broken.");
        }
        byte[][] parts = ByteFiddling.split(nmea, from + 1, to - 2, (byte) ',');
        byte[] chkpt = parts[parts.length - 1];
        Integer actualsum = ByteFiddling.parseInt(chkpt, 1, chkpt.length);
        if (chkpt[0] != '*' || actualsum == null) {
            throw new IOException("NMEA checksum not found.");
        }
        int checksum = checksum(nmea, from + 1, to - 2 - chkpt.length);
        if (checksum != actualsum) {
            throw new IOException("NMEA checksum mismatch: " + actualsum + " calculated instead of " + checksum);
        }
        return parts;
    }

    private static int checksum(byte[] nmea, int start, int end) {
        byte out = 0;
        for (int i = start; i < end; i++) {
            out ^= nmea[i];
        }
        return out;
    }
}
