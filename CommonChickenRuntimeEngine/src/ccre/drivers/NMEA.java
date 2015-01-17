package ccre.drivers;

import java.io.IOException;

public class NMEA {

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

    // This is from the UM7LT. If this is wrong for another device, perhaps we need multiple versions.
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
        for (int i=start; i<end; i++) {
            out ^= nmea[i];
        }
        return out;
    }
}
