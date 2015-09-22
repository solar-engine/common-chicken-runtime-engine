/*
 * Copyright 2013-2015 Colby Skeggs
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
package ccre.log;

import java.io.Serializable;

/**
 * Represents a Logging level. This represents how important/severe a logging
 * message is. The levels are, in order of descending severity: severe, warning,
 * info, config, fine, finer, finest.
 *
 * @author skeggsc
 */
public class LogLevel implements Serializable, Comparable<LogLevel> {

    private static final long serialVersionUID = -4814366160212981658L;

    public static final byte FATAL_ID = 9;
    public static final byte ERROR_ID = 6;
    public static final byte WARN_ID = 3;
    public static final byte INFO_ID = 0;
    public static final byte DEBUG_ID = -3;
    public static final byte TRACE_ID = -6;

    /**
     * A severe error. This usually means that something major didn't work, or
     * an impossible condition occurred.
     */
    public static final LogLevel FATAL = new LogLevel(FATAL_ID, "FATAL");

    /**
     * An error. This usually means that something bad happened, but some things
     * may still work.
     */
    public static final LogLevel ERROR = new LogLevel(ERROR_ID, "ERROR");

    /**
     * A warning. This usually means that something that could cause a potential
     * problem happened, but things should still work.
     */
    public static final LogLevel WARN = new LogLevel(ERROR_ID, "WARN");

    /**
     * A piece of info. This usually means something happened that the user
     * might want to know.
     */
    public static final LogLevel INFO = new LogLevel(INFO_ID, "INFO");

    /**
     * A debugging message. This can be caused by anything, but probably
     * shouldn't be logged particularly often.
     */
    public static final LogLevel DEBUG = new LogLevel(DEBUG_ID, "DEBUG");

    /**
     * A trace message. This could be caused by anything, but probably shouldn't
     * be logged particularly often.
     */
    public static final LogLevel TRACE = new LogLevel(TRACE_ID, "TRACE");

    private static final LogLevel[] levels = new LogLevel[] { TRACE, DEBUG, INFO, WARN, ERROR, FATAL };

    /**
     * Get a LogLevel from its ID level. If it doesn't exist, a RuntimeException
     * is thrown. Should probably only be called on the result of toByte.
     *
     * @param id the ID of the LogLevel.
     * @return the LogLevel with this ID.
     * @see #id
     * @see #toByte(ccre.log.LogLevel)
     */
    public static LogLevel fromByte(byte id) {
        if ((id + 9) % 3 != 0 || id < -9 || id > 9) {
            throw new RuntimeException("Invalid LogLevel ID: " + id);
        }
        return levels[(id + 9) / 3];
    }

    /**
     * Return a byte representing this logging level - that is, its ID. Used in
     * fromByte.
     *
     * @param level the LogLevel to serialize.
     * @return the byte version of the LogLevel.
     * @see #id
     * @see #fromByte(byte)
     */
    public static byte toByte(LogLevel level) {
        return level.id;
    }

    /**
     * The ID of the LogLevel. The higher, the more severe. SEVERE is 9, FINEST
     * is -9, for example.
     */
    public final byte id;
    /**
     * The long-form message representing this level.
     */
    public final String message;

    private LogLevel(int id, String msg) {
        this.id = (byte) id;
        if (id != this.id) {
            throw new IllegalArgumentException();
        }
        message = msg;
    }

    /**
     * Check if this logging level is at least as important/severe as the other
     * logging level.
     *
     * @param other the logging level to compare to.
     * @return if this is at least as important.
     */
    public boolean atLeastAsImportant(LogLevel other) {
        return id >= other.id;
    }

    /**
     * Convert this LogLevel to a string. Returns the message.
     *
     * @return the message.
     */
    @Override
    public String toString() {
        return message;
    }

    private Object readResolve() {
        return fromByte(id);
    }

    /**
     * Get the next (more severe) LogLevel, or the least severe if the current
     * level is the most severe.
     *
     * The idea is that this can be used in a user interface to iterate around
     * the list of LogLevels.
     *
     * @return the next LogLevel.
     */
    public LogLevel next() {
        for (int i = 0; i < levels.length - 1; i++) {
            if (levels[i] == this) {
                return levels[i + 1];
            }
        }
        return levels[0];
    }

    @Override
    public int compareTo(LogLevel o) {
        if (o.id > this.id)
            return -1;
        if (o.id < this.id)
            return +1;
        return 0; // they are equal
    }
}
