/*
 * Copyright 2013-2014 Colby Skeggs
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
package ccre.testing;

/**
 * An exception thrown when a test fails. This is thrown by the various
 * assert... methods, you probably shouldn't throw one yourself.
 *
 * @author skeggsc
 */
@SuppressWarnings("serial")
public class TestingException extends Exception {

    /**
     * Creates a TestingException with no message.
     */
    public TestingException() {
        super();
    }

    /**
     * Creates an TestingException with a specified message.
     *
     * @param message The specified message.
     */
    public TestingException(String message) {
        super(message);
    }
}
