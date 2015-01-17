package ccre.drivers;

public class ByteFiddling {

    public static int indexOf(byte[] bytes, int from, int to, byte b) {
        for (int i = from; i < to; i++) {
            if (bytes[i] == b) {
                return i;
            }
        }
        return -1;
    }

    public static byte[] sub(byte[] bytes, int from, int to) {
        byte[] out = new byte[to - from];
        System.arraycopy(bytes, from, out, 0, to - from);
        return out;
    }

    public static Integer parseInt(byte[] bytes) {
        return parseInt(bytes, 0, bytes.length);
    }

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

    public static int count(byte[] bytes, byte b) {
        int count = 0;
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == b) {
                count++;
            }
        }
        return count;
    }

    public static byte[][] split(byte[] bytes, int from, int to, byte b) {
        byte[][] out = new byte[count(bytes, b) + 1][];
        for (int i = 0; i < out.length; i++) {
            int next = indexOf(bytes, from, to, b);
            out[i] = sub(bytes, from, next);
            from = next + 1;
        }
        return out;
    }

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

    public static Double parseDouble(byte[] bytes) {
        return parseDouble(bytes, 0, bytes.length);
    }

    public static Double parseDouble(byte[] bytes, int from, int to) {
        try {
            return Double.parseDouble(parseASCII(bytes, from, to));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static String parseASCII(byte[] bytes) {
        return parseASCII(bytes, 0, bytes.length);
    }

    public static String parseASCII(byte[] bytes, int from, int to) {
        char[] conv = new char[to - from];
        for (int i = from, j = 0; i < to; i++, j++) {
            conv[j] = (char) (bytes[i] & 0xFF);
        }
        return new String(conv);
    }

    public static String toHex(byte[] bytes, int from, int to) {
        char[] out = new char[2 * (to - from)];
        for (int i = from; i < to; i++) {
            out[i * 2] = hex[bytes[i] >> 4];
            out[i * 2 + 1] = hex[bytes[i] & 0xF];
        }
        return new String(out);
    }
    
    private static final char[] hex = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
}
