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
package ccre.ctrl;

/**
 * An exception thrown when an ExtendedMotor cannot complete the requested
 * operation.
 *
 * @author skeggsc
 */
public class ExtendedMotorFailureException extends Exception {

    private static final long serialVersionUID = 8628319463301729456L;
    private final Throwable cause;

    /**
     * Creates a new ExtendedMotorFailureException, with no message or cause.
     */
    public ExtendedMotorFailureException() {
        super();
        this.cause = null;
    }

    /**
     * Creates a new ExtendedMotorFailureException, with a message and no cause.
     *
     * @param message the message of the exception.
     */
    public ExtendedMotorFailureException(String message) {
        super(message);
        this.cause = null;
    }

    /**
     * Creates a new ExtendedMotorFailureException, with a cause and no message.
     *
     * @param cause the cause of the exception.
     */
    public ExtendedMotorFailureException(Throwable cause) {
        super();
        this.cause = cause;
        this.initCause(cause);
    }

    /**
     * Creates a new ExtendedMotorFailureException, with both a message and a
     * cause.
     *
     * @param message the message of the exception.
     * @param cause the cause of the exception.
     */
    public ExtendedMotorFailureException(String message, Throwable cause) {
        super(message);
        this.cause = cause;
        this.initCause(cause);
    }

    /**
     * Gets the cause of the Throwable in a way that works even on the cRIO,
     * which doesn't allow for normally-associated causes.
     *
     * @return the cause, or null if none exists.
     */
    public Throwable getEMCause() {
        return cause;
    }
}
