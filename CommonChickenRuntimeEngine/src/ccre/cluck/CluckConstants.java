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
package ccre.cluck;

/**
 * A class containing the "RMT" types for Cluck - the possible meanings of the
 * very first byte in a Cluck message. Also contains other constants.
 * 
 * Also provides {@link #rmtToString(int)}, which gives names to these message
 * types.
 * 
 * @author skeggsc
 */
public class CluckConstants {

    /**
     * The destination string used for broadcast messages.
     */
    public static final String BROADCAST_DESTINATION = "*";
    /**
     * The ID representing a PING message.
     */
    public static final byte RMT_PING = 0;
    /**
     * The ID representing an EventOutput firing message.
     */
    public static final byte RMT_EVENTOUTP = 1;
    /**
     * The ID representing an EventInput subscription message.
     */
    public static final byte RMT_EVENTINPUT = 2;
    /**
     * The ID representing an EventInput response message.
     */
    public static final byte RMT_EVENTINPUTRESP = 3;
    /**
     * The ID representing a logging message.
     */
    public static final byte RMT_LOGTARGET = 4;
    /**
     * The ID representing a BooleanInput subscription message.
     */
    public static final byte RMT_BOOLINPUT = 5;
    /**
     * The ID representing a BooleanInput response message.
     */
    public static final byte RMT_BOOLINPUTRESP = 6;
    /**
     * The ID representing a BooleanOutput write message.
     */
    public static final byte RMT_BOOLOUTP = 7;
    /**
     * The ID representing a FloatInput subscription message.
     */
    public static final byte RMT_FLOATINPUT = 8;
    /**
     * The ID representing a FloatInput response message.
     */
    public static final byte RMT_FLOATINPUTRESP = 9;
    /**
     * The ID representing a FloatOutput write message.
     */
    public static final byte RMT_FLOATOUTP = 10;
    /**
     * The ID representing an OutputStream write message.
     */
    public static final byte RMT_OUTSTREAM = 11;
    /**
     * The ID representing a network infrastructure modification notification.
     */
    public static final byte RMT_NOTIFY = 12;
    /**
     * The ID representing a remote procedure invocation.
     */
    public static final byte RMT_INVOKE = 13;
    /**
     * The ID representing a response to a remote procedure invocation.
     */
    public static final byte RMT_INVOKE_REPLY = 14;
    /**
     * The ID representing a notification that a link doesn't exist.
     */
    public static final byte RMT_NEGATIVE_ACK = 15;
    /**
     * The ID representing an EventInput unsubscription request.
     */
    public static final byte RMT_LEGACY_EVENTINPUT_UNSUB = 16;
    /**
     * The ID representing an BooleanInput unsubscription request.
     */
    public static final byte RMT_LEGACY_BOOLINPUT_UNSUB = 17;
    /**
     * The ID representing an FloatInput unsubscription request.
     */
    public static final byte RMT_LEGACY_FLOATINPUT_UNSUB = 18;
    /**
     * The total number of used RMTs.
     */
    public static final byte COUNT_RMTS = 19;
    private static final String[] remoteNames = new String[] { "Ping", "EventOutput", "EventInput", "EventInputResponse", "LogTarget", "BooleanInput", "BooleanInputResponse", "BooleanOutput", "FloatInput", "FloatInputResponse", "FloatOutput", "OutputStream", "Notify", "RemoteProcedure", "RemoteProcedureReply", "NonexistenceNotification", "LEGACY_EventInputUnsubscription", "LEGACY_BooleanInputUnsubscription", "LEGACY_FloatInputUnsubscription" };

    /**
     * Convert an RMT ID to a string.
     *
     * @param type The RMT_* message ID.
     * @return The version representing the name of the message type.
     */
    public static String rmtToString(int type) {
        if (type >= 0 && type < remoteNames.length) {
            return remoteNames[type];
        } else {
            return "Unknown #" + type;
        }
    }

    private CluckConstants() {
    }

}
