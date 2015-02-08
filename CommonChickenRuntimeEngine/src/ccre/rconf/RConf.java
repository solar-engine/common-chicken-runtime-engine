package ccre.rconf;

import ccre.channel.BooleanInputPoll;
import ccre.channel.FloatInputPoll;

/**
 * The RConf subsystem's utility class.
 * 
 * The basic idea of RConf is best understood like a web browser or other client
 * - the client (usually the PoultryInspector) requests a snapshot of data from
 * an RConfable device, and then displays it to the user once received.
 * 
 * The user can then interact with certain components of the result in certain
 * ways, and this sends data back to the original RConfable. (The RConfable, of
 * course, doesn't have to do anything with this data.)
 * 
 * @author skeggsc
 */
public class RConf {

    /**
     * The type of a TITLE component. This has large text, and is used for
     * clearly describing the device.
     */
    public static final byte F_TITLE = 0;
    /**
     * The type of a BOOLEAN component. This can be either true or false, and
     * the user can set it to true or false remotely if the RConfable wants.
     */
    public static final byte F_BOOLEAN = 1;
    /**
     * The type of an INTEGER component. This can be any integer, and the user
     * can modify it remotely if the RConfable wants.
     */
    public static final byte F_INTEGER = 2;
    /**
     * The type of a FLOAT component. This can be any float, and the user can
     * modify it remotely if the RConfable wants.
     */
    public static final byte F_FLOAT = 3;
    /**
     * The type of a STRING component. This is displayed with normal-sized text,
     * and the user can modify it remotely if the RConfable wants.
     */
    public static final byte F_STRING = 4;
    /**
     * The type of a BUTTON component. This is displayed with a border, and the
     * user can press it.
     */
    public static final byte F_BUTTON = 5;
    /**
     * The type of a CLUCK reference. This allows for, essentially, a "link" to
     * another entry on the Cluck network. The path is relative to the
     * RConfable's publishing location.
     */
    public static final byte F_CLUCK_REF = 6;

    public static interface Entry {
        public byte[] encode();
    }

    public static String parseTextual(byte[] entry) {
        return entry.length >= 1 ? new String(entry, 1, entry.length - 1) : null;
    }

    public static Boolean parseBoolean(byte[] entry) {
        return entry.length >= 2 ? entry[1] != 0 : null;
    }

    public static Integer parseInteger(byte[] entry) {
        return entry.length >= 5 ? (((entry[1] & 0xFF) << 24) | ((entry[2] & 0xFF) << 16) | ((entry[3] & 0xFF) << 8) | (entry[4] & 0xFF)) : null;
    }

    public static Float parseFloat(byte[] entry) {
        Integer i = parseInteger(entry);
        return i == null ? null : Float.intBitsToFloat(i);
    }

    public static Entry title(String title) {
        return fixed(F_TITLE, title.getBytes());
    }

    public static Entry string(String data) {
        return fixed(F_STRING, data.getBytes());
    }

    public static Entry button(String label) {
        return fixed(F_BUTTON, label.getBytes());
    }

    public static Entry cluckRef(String ref) {
        return fixed(F_CLUCK_REF, ref.getBytes());
    }

    public static Entry fieldInteger(int i) {
        return fixed(integerAsBytesWithPrefix(F_INTEGER, i));
    }

    public static Entry fieldFloat(final FloatInputPoll b) {
        return new Entry() {
            public byte[] encode() {
                return integerAsBytesWithPrefix(F_FLOAT, Float.floatToIntBits(b.get()));
            }
        };
    }

    public static Entry fieldFloat(float b) {
        return fixed(integerAsBytesWithPrefix(F_FLOAT, Float.floatToIntBits(b)));
    }

    private static byte[] integerAsBytesWithPrefix(byte prefix, int b) {
        return new byte[] { prefix, (byte) (b >> 24), (byte) (b >> 16), (byte) (b >> 8), (byte) b };
    }

    public static Entry fieldBoolean(final BooleanInputPoll b) {
        return new Entry() {
            public byte[] encode() {
                return new byte[] { F_BOOLEAN, b.get() ? (byte) 1 : (byte) 0 };
            }
        };
    }

    public static Entry fieldBoolean(boolean b) {
        return fixed(F_BOOLEAN, b ? (byte) 1 : (byte) 0);
    }

    public static Entry fixed(byte type, byte[] data) {
        byte[] out = new byte[data.length + 1];
        System.arraycopy(data, 0, out, 1, data.length);
        out[0] = type;
        return fixed(out);
    }

    public static Entry fixed(final byte... data) {
        return new Entry() {
            public byte[] encode() {
                return data;
            }
        };
    }

    public static String toString(Entry e) {
        byte[] data = e.encode();
        if (data.length < 1) {
            return "<invalid:too-short>";
        }
        switch (data[0]) {
        case F_TITLE:
            String title = parseTextual(data);
            return title == null ? "<invalid:bad-title>" : "# " + title;
        case F_BOOLEAN:
            Boolean b = parseBoolean(data);
            return b == null ? "<invalid:bad-bool>" : b.toString();
        case F_INTEGER:
            Integer i = parseInteger(data);
            return i == null ? "<invalid:bad-int>" : i.toString();
        case F_FLOAT:
            Float f = parseFloat(data);
            return f == null ? "<invalid:bad-float>" : f.toString();
        case F_STRING:
            String text = parseTextual(data);
            return text == null ? "<invalid:bad-str>" : text;
        case F_BUTTON:
            String label = parseTextual(data);
            return label == null ? "<invalid:bad-label>" : "[" + label + "]";
        case F_CLUCK_REF:
            String ref = parseTextual(data);
            return ref == null ? "<invalid:bad-cluck>" : "@" + ref;
        default:
            return "<invalid:bad-type>";
        }
    }

    public static float bytesToFloat(byte[] data) {
        return Float.intBitsToFloat(((data[0] & 0xFF) << 24) | ((data[1] & 0xFF) << 16) | ((data[2] & 0xFF) << 8) | (data[3] & 0xFF));
    }

    private RConf() {
    }
}
