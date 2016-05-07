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
package ccre.rconf;

import java.io.Serializable;

import ccre.util.Utils;
import ccre.verifier.IgnoredPhase;
import ccre.verifier.SetupPhase;

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
    /**
     * The type of an AUTOREFERENCE reference. This strongly implies to the
     * client that it should refresh every some number of milliseconds.
     */
    public static final byte F_AUTO_REFRESH = 7;

    /**
     * An entry as part of the result from querying an RConfable. Could be any
     * of the types available statically on RConf - such as F_TITLE, F_BUTTON,
     * F_INTEGER, or others.
     *
     * Contains a type and a byte array of the associated data.
     *
     * @author skeggsc
     */
    public static final class Entry implements Serializable {

        private static final long serialVersionUID = -5212798086046991238L;
        /**
         * The type of this entry. See the static fields on RConf.
         */
        public final byte type;
        /**
         * The contents of this entry. The meaning depends on the type.
         */
        public final byte[] contents;

        /**
         * Create a new RConf Entry with a type and some contents.
         *
         * @param type the type of the entry.
         * @param contents the byte array contents of the entry.
         */
        public Entry(byte type, byte... contents) {
            this.type = type;
            this.contents = contents;
        }

        /**
         * Parse the contents of this Entry as a textual value.
         *
         * @return the string representing the contents.
         * @throws IllegalStateException if the type of this entry is not
         * supposed to contain text.
         */
        @IgnoredPhase
        public String parseTextual() throws IllegalStateException {
            if (type != F_TITLE && type != F_STRING && type != F_BUTTON && type != F_CLUCK_REF) {
                throw new IllegalStateException("Invalid type of Entry in parseTextual: " + type);
            }
            return Utils.fromBytes(contents, 0, contents.length);
        }

        /**
         * Parse the contents of this Entry as a boolean value.
         *
         * @return the boolean representing the contents, or null if invalid.
         * @throws IllegalStateException if the type of this entry is not
         * supposed to contain a boolean.
         */
        @IgnoredPhase
        public Boolean parseBoolean() {
            if (type != F_BOOLEAN) {
                throw new IllegalStateException("Invalid type of Entry in parseBoolean: " + type);
            }
            return contents.length >= 1 ? contents[0] != 0 : null;
        }

        /**
         * Parse the contents of this Entry as an integer value.
         *
         * @return the integer representing the contents, or null if invalid.
         * @throws IllegalStateException if the type of this entry is not
         * supposed to contain a integer.
         */
        @IgnoredPhase
        public Integer parseInteger() {
            if (type != F_INTEGER && type != F_AUTO_REFRESH) {
                throw new IllegalStateException("Invalid type of Entry in parseInteger: " + type);
            }
            return getAsInteger();
        }

        /**
         * Parse the contents of this Entry as a float value.
         *
         * @return the float representing the contents, or null if invalid.
         * @throws IllegalStateException if the type of this entry is not
         * supposed to contain a float.
         */
        @IgnoredPhase
        public Float parseFloat() {
            if (type != F_FLOAT) {
                throw new IllegalStateException("Invalid type of Entry in parseFloat: " + type);
            }
            return Float.intBitsToFloat(getAsInteger());
        }

        @IgnoredPhase
        private Integer getAsInteger() {
            return contents.length >= 4 ? (((contents[0] & 0xFF) << 24) | ((contents[1] & 0xFF) << 16) | ((contents[2] & 0xFF) << 8) | (contents[3] & 0xFF)) : null;
        }

        @Override
        public String toString() {
            switch (type) {
            case F_TITLE:
                String title = parseTextual();
                return title == null ? "<invalid:bad-title>" : "# " + title;
            case F_BOOLEAN:
                Boolean b = parseBoolean();
                return b == null ? "<invalid:bad-bool>" : b.toString();
            case F_INTEGER:
                Integer i = parseInteger();
                return i == null ? "<invalid:bad-int>" : i.toString();
            case F_FLOAT:
                Float f = parseFloat();
                return f == null ? "<invalid:bad-float>" : f.toString();
            case F_STRING:
                String text = parseTextual();
                return text == null ? "<invalid:bad-str>" : text;
            case F_BUTTON:
                String label = parseTextual();
                return label == null ? "<invalid:bad-label>" : "[" + label + "]";
            case F_CLUCK_REF:
                String ref = parseTextual();
                return ref == null ? "<invalid:bad-cluck>" : "@" + ref;
            case F_AUTO_REFRESH:
                Integer timeout = parseInteger();
                return timeout == null ? "<invalid:bad-auto-refresh>" : "<meta:auto-refresh:" + timeout + ">";
            default:
                return "<invalid:bad-type>";
            }
        }
    }

    /**
     * Create a new TITLE component with the given textual contents.
     *
     * @param title the title information.
     * @return the new RConf entry.
     */
    @SetupPhase
    public static Entry title(String title) {
        return new Entry(F_TITLE, Utils.getBytes(title));
    }

    /**
     * Create a new STRING component with the given textual contents.
     *
     * @param data the textual information.
     * @return the new RConf entry.
     */
    @SetupPhase
    public static Entry string(String data) {
        return new Entry(F_STRING, Utils.getBytes(data));
    }

    /**
     * Create a new BUTTON component with the given textual label.
     *
     * @param label the button label.
     * @return the new RConf entry.
     */
    @SetupPhase
    public static Entry button(String label) {
        return new Entry(F_BUTTON, Utils.getBytes(label));
    }

    /**
     * Create a new CLUCK REFERENCE component with the given Cluck path,
     * relative to where the RConfable in which this is used will be published.
     *
     * @param ref the relative path.
     * @return the new RConf entry.
     */
    @SetupPhase
    public static Entry cluckRef(String ref) {
        return new Entry(F_CLUCK_REF, Utils.getBytes(ref));
    }

    /**
     * Create a new INTEGER component with the given integer content.
     *
     * @param integer the integer content.
     * @return the new RConf entry.
     */
    @SetupPhase
    public static Entry fieldInteger(int integer) {
        return new Entry(F_INTEGER, integerAsBytes(integer));
    }

    /**
     * Create a new FLOAT component with the given float content.
     *
     * @param f the float content.
     * @return the new RConf entry.
     */
    @SetupPhase
    public static Entry fieldFloat(float f) {
        return new Entry(F_FLOAT, integerAsBytes(Float.floatToIntBits(f)));
    }

    @IgnoredPhase
    private static byte[] integerAsBytes(int b) {
        return new byte[] { (byte) (b >> 24), (byte) (b >> 16), (byte) (b >> 8), (byte) b };
    }

    /**
     * Create a new BOOLEAN component with the given boolean value.
     *
     * @param bool the boolean value.
     * @return the new RConf entry.
     */
    @SetupPhase
    public static Entry fieldBoolean(boolean bool) {
        return new Entry(F_BOOLEAN, bool ? (byte) 1 : (byte) 0);
    }

    /**
     * Create a new AUTO_REFRESH component with the given boolean value.
     *
     * @param timeout the timeout, in milliseconds.
     * @return the new RConf entry.
     */
    @SetupPhase
    public static Entry autoRefresh(int timeout) {
        return new Entry(F_AUTO_REFRESH, integerAsBytes(timeout));
    }

    /**
     * A helper method to convert bytes into a float.
     *
     * @param data the four bytes of data to convert.
     * @return the resulting float.
     */
    @IgnoredPhase
    public static float bytesToFloat(byte[] data) {
        return Float.intBitsToFloat(((data[0] & 0xFF) << 24) | ((data[1] & 0xFF) << 16) | ((data[2] & 0xFF) << 8) | (data[3] & 0xFF));
    }

    private RConf() {
    }
}
