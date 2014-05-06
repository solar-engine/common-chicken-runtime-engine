/*
 * Copyright 2014 Colby Skeggs
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
package ccre.downgrade;

/**
 * This is the same as java.io.FileNotFoundException. Don't use this. It is used
 * when Retrotranslator downgrades the code to 1.3, because 1.3 doesn't have
 * FileNotFoundException.
 *
 * @see java.io.FileNotFoundException
 * @author skeggsc
 */
@SuppressWarnings("serial")
public class FileNotFoundException extends RuntimeException {

    /**
     * Creates a FileNotFoundException with no message.
     */
    public FileNotFoundException() {
    }

    /**
     * Creates an FileNotFoundException with a specified message.
     *
     * @param message The specified message.
     */
    public FileNotFoundException(String message) {
        super(message);
    }
}
