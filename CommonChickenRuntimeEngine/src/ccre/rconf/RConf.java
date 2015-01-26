package ccre.rconf;

import ccre.channel.BooleanInputPoll;
import ccre.channel.FloatInputPoll;
import ccre.rconf.RConf.Entry;

public class RConf {

    public static final byte F_TITLE = 0;
    public static final byte F_BOOLEAN = 1;
    public static final byte F_INTEGER = 2;
    public static final byte F_FLOAT = 3;
    public static final byte F_STRING = 4;
    public static final byte F_BUTTON = 5;
    public static final byte F_CLUCK_REF = 6;

    public static interface Entry {
        public byte[] encode();
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
        return new byte[] {(byte) (b >> 24), (byte) (b >> 16), (byte) (b >> 8), (byte) b};
    }
    
    public static Entry fieldBoolean(final BooleanInputPoll b) {
        return new Entry() {
            public byte[] encode() {
                return new byte[] {F_BOOLEAN, b.get() ? (byte) 1 : (byte) 0};
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
}
